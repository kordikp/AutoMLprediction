package game.classifiers.ensemble;

import game.classifiers.Classifier;
import game.classifiers.evolution.EvolvableClassifier;
import game.evolution.ObjectEvolvable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.GAMEClassifierConfig;

/**
 * Classifier ensemble hierarchical evolver for population of Classifiers represented by Genome
 */
public class ClassifierGAME extends ClassifierEvolvableEnsemble {
    static Logger logger = Logger.getLogger(ClassifierGAME.class);
    Vector<Vector<EvolvableClassifier>> layers;
    boolean increasingComplexity;
    boolean probabilities;
    int globalInputsNumber;

    @Override
    public void init(ClassifierConfig cfg) {
        layers = new Vector<Vector<EvolvableClassifier>>();
        this.increasingComplexity = ((GAMEClassifierConfig) cfg).isIncreasingComplexity();
        this.probabilities = ((GAMEClassifierConfig) cfg).isPropabilities();
        super.init(cfg);
        if (increasingComplexity) actualLayer = 1; //only one input allowed in the first layer
    }


    public void learn() {

        learnVectNum = learning_vectors * learnValidRatio / 100;
        validVectNum = learning_vectors - learnVectNum;
        prepareLearningAndValidationData();
        logger.info("Data prepared");
        createBaseClassifiers();
        logger.info("Initial population generated");
        int layer_index = 0;
        double fittest = Double.MIN_VALUE;
        globalInputsNumber = inputs;
        do {

            logger.info("Layer " + layer_index + " evolution starts");
            logger.info("Number of generations: " + generations);
            if (layers.size() > 0) { //skip for first layer
                int extraInputs = layers.lastElement().size();
                if (probabilities) extraInputs *= outputs;
                inputs += extraInputs;
                ensClassifiers.clear();
                createBaseClassifiers();
                updateInputVectors();
            }
            logger.info("Initial population generated");
            computeFitness((ArrayList<EvolvableClassifier>) ensClassifiers);
            for (int generation = 0; generation < generations; generation++) {
                logger.debug("Generation " + generation);
                ensClassifiers = evolution.newGeneration((ArrayList<EvolvableClassifier>) ensClassifiers);
            }
            ensClassifiers = evolution.getFinalPopulation((ArrayList<EvolvableClassifier>) ensClassifiers);
            logger.info("Layer " + layer_index + " evolution ends, surviving Classifiers selected");
            layer_index++;
            double layer_fittest = ((ObjectEvolvable) ensClassifiers.get(0)).getFitness();
            logger.info("Fitness:" + layer_fittest);
            if (Double.isNaN(layer_fittest)) {
                logger.info("Fitness of new layer is NaN, GAME terminates!");
                ensClassifiers.clear();
                break; // not improving, delete last layer and stop
            }
            if (layer_fittest <= fittest) {
                logger.info("Fitness of new layer not improved best-so-far fitness, GAME terminates! (difference:" + (layer_fittest - fittest));
                ensClassifiers.clear();
                break; // not improving, delete last layer and stop
            }

            fittest = layer_fittest;
            Vector<EvolvableClassifier> survivingClassifiers = new Vector<EvolvableClassifier>();
            for (Classifier classifier : ensClassifiers) {
                survivingClassifiers.add((EvolvableClassifier) classifier);
            }   //copy Classifiers
            layers.add(survivingClassifiers);
            if (increasingComplexity) actualLayer++;
        } while (true);
        //ensure that the last layer contains just one Classifier - the output
        if ((layers == null) || (layers.isEmpty()) || (layers.lastElement() == null)) {
            learned = false;
            return;
        }
        EvolvableClassifier output = layers.lastElement().firstElement();
        layers.lastElement().clear();
        layers.lastElement().add(output);
        learned = true;
    }

    /**
     * Updates inputVectors (input vectors of the learning matrix) with outputs of Classifiers from the last layer
     */
    private void updateInputVectors() {
        double[][] newInputVect = new double[maxLearningVectors][inputs];
        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(inputVect[i], 0, newInputVect[i], 0, inputVect[i].length);
            int index = inputVect[i].length;
            for (Classifier model : layers.lastElement()) {
                if (probabilities) {
                    double[] props = model.getOutputProbabilities(inputVect[i]);
                    System.arraycopy(props, 0, newInputVect[i], index, outputs);
                } else newInputVect[i][index] = model.getOutput(inputVect[i]);
            }
        }
        inputVect = newInputVect;
    }


    /**
     * Returns hierarchical combination of surviving Classifiers
     *
     * @param input_vector Specify inputs to the Classifier
     * @return output of the GAME hierarchical ensemble
     */
   /* public int getOutput(double[] input_vector) {
        double[] out = getOutputProbabilities(input_vector);

    }  */
    public double[] getOutputProbabilities(double[] input_vector) {
        Vector<Integer> outi = new Vector<Integer>();
        Vector<Vector<Double>> outp = new Vector<Vector<Double>>();
        double[] output = null;
        for (Vector<EvolvableClassifier> layer : layers) {
            int outs;
            if (probabilities) {
                outs = outp.size() * outputs;
            } else outs = outi.size();

            double[] input = new double[input_vector.length + outs];
            System.arraycopy(input_vector, 0, input, 0, input_vector.length);
            int index = input_vector.length;

            if (probabilities) {
                for (Vector<Double> props : outp) {
                    for (Double prop : props) {
                        input[index++] = prop;
                    }
                }
            } else {
                for (Integer out : outi) {
                    input[index++] = out;
                }
            }
            for (Classifier classifier : layer) {
                if (probabilities) {
                    Vector<Double> v = new Vector<Double>();
                    output = classifier.getOutputProbabilities(input);
                    for (int i = 0; i < output.length; i++) {
                        v.add(output[i]);
                    }
                    outp.add(v);
                } else {
                    outi.add(classifier.getOutput(input));
                }
            }
        }
        // there is only one classifier in the last layer so the final output should be the last output extracted
        if (output == null) return null;
        return output;
    }

    /*
    private Vector<Integer> getOutputs(double[] input_vector) {
        Vector<Integer> outputs = new Vector<Integer>();

        for (Vector<EvolvableClassifier> layer : layers) {   //todo probabilities instead of class indexes
            double[] input = new double[input_vector.length + outputs.size()];
            System.arraycopy(input_vector, 0, input, 0, input_vector.length);
            int index = input_vector.length;
            for (Integer out : outputs) {
                input[index++] = out;
            }
            for (Classifier classifier : layer) {
                outputs.add(classifier.getOutput(input));
            }
        }
        return outputs;
    }
    */
    /* private Vector<Vector<Double>> getAllOutputProbabilities(double[] input_vector) {
        Vector<Vector<Double>> outputs = new Vector<Vector<Double>>();

        for (Vector<EvolvableClassifier> layer : layers) {
            double[] input = new double[input_vector.length + outputs.size()];
            System.arraycopy(input_vector, 0, input, 0, input_vector.length);
            int index = input_vector.length;
            for (Integer out : outputs) {
                input[index++] = out;
            }
            for (Classifier classifier : layer) {
                outputs.add(classifier.getOutputProbabilities(input));
            }
        }
        return outputs;
    }*/
    public String toEquation(String[] inputEquation) {
        Vector<String> outputsE = new Vector<String>();

        for (Vector<EvolvableClassifier> layer : layers) {
            int addition;
            if (probabilities) addition = outputsE.size();
            else addition = outputsE.size();
            String[] input = new String[inputEquation.length + addition];
            System.arraycopy(inputEquation, 0, input, 0, inputEquation.length);
            int index = inputEquation.length;
            for (String out : outputsE) {
                input[index++] = out;
            }
            for (Classifier model : layer) {
                if (probabilities) {
                    String[] eq = model.getEquations(input);
                    for (int i = 0; i < outputs; i++)
                        outputsE.add(eq[i]);
                } else outputsE.add(model.toEquation(input));
            }
        }
        if (probabilities) {
            List r = outputsE.subList(outputsE.size() - outputs, outputsE.size());
            return r.toString();
        } else return outputsE.lastElement();
    }

    public Class getConfigClass() {
        return GAMEClassifierConfig.class;
    }
}