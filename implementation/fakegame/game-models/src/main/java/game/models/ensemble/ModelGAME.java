package game.models.ensemble;

import game.evolution.ObjectEvolvable;
import game.models.Model;
import game.models.evolution.ModelEvolvable;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import configuration.models.ModelConfig;
import configuration.models.ensemble.GAMEEnsembleModelConfig;

/**
 * Model ensemble hierarchical evolver for population of models represented by Genome
 */
public class ModelGAME extends ModelEvolvableEnsemble {
    static Logger logger = Logger.getLogger(ModelGAME.class);
    Vector<Vector<ModelEvolvable>> layers;
    boolean increasingComplexity;
    int globalInputsNumber;
    int maxLayers;

    @Override
    public void init(ModelConfig cfg) {
        layers = new Vector<Vector<ModelEvolvable>>();
        this.increasingComplexity = ((GAMEEnsembleModelConfig) cfg).isIncreasingComplexity();
        this.maxLayers = ((GAMEEnsembleModelConfig) cfg).getMaxLayers();
        super.init(cfg);
        if (increasingComplexity) maxInputs = 1;
    }


    public void learn() {
        logger.info("Initial population generated");
        learnVectNum = learning_vectors * learnValidRatio / 100;
        validVectNum = learning_vectors - learnVectNum;
        prepareLearningAndValidationData();
        logger.info("Data prepared");
        int layer_index = 0;
        double fittest = Double.MIN_VALUE;
        globalInputsNumber = inputsNumber;
        double firstLayerFit = -1;
        double lastLayerFit = -1;
        //   createBaseModels();
        do {

            logger.info("Layer " + layer_index + " evolution starts");
            logger.info("Number of generations: " + generations);
            if (layers.size() > 0) { //skip for first layer
                inputsNumber += layers.lastElement().size();
                updateInputVectors();
                double[][] iv = inputVect;
                double[] t = target;   //backup data
                ensembleModels.clear();
                createBaseModels();
                setInputsNumber(inputsNumber);
                inputVect = iv;
                target = t; //restore data initialized by setInputsNumber()

            }
            logger.info("Initial population generated");
            computeFitness((ArrayList<ModelEvolvable>) ensembleModels);
            for (int generation = 0; generation < generations; generation++) {
                logger.debug("Generation " + generation);
                ensembleModels = evolution.newGeneration((ArrayList<ModelEvolvable>) ensembleModels);
            }
            ensembleModels = evolution.getFinalPopulation((ArrayList<ModelEvolvable>) ensembleModels);
            logger.info("Layer " + layer_index + " evolution ends, surviving models selected");
            layer_index++;
            double layer_fittest = ((ObjectEvolvable) ensembleModels.get(0)).getFitness();
            logger.info("Fitness:" + layer_fittest);
            if (layer_fittest <= fittest) {
                logger.info("Fitness of new layer not improved best-so-far fitness, GAME terminates! (difference:" + (layer_fittest - fittest));
                ensembleModels.clear();
                break; // not improving, delete last layer and stop
            }

            fittest = layer_fittest;
            Vector<ModelEvolvable> survivingModels = new Vector<ModelEvolvable>();
            for (Model model : ensembleModels) {
                survivingModels.add((ModelEvolvable) model);
            }   //copy models
            layers.add(survivingModels);
            if ((maxLayers > 0) && (layers.size() >= maxLayers)) break;
            if (increasingComplexity) maxInputs++;
            if (layers.size() < 2) {
                firstLayerFit = fittest;
            } else {
                if (((fittest - lastLayerFit) * 30.0) < (lastLayerFit - firstLayerFit)) {
                    logger.info("Fitness of new layer is not significantly higher than that of the previous layer!");
                    break; // too small improvement
                }

            }
            lastLayerFit = fittest;
        } while (true);
        //ensure that the last layer contains just one Model - the output
        ModelEvolvable output = layers.lastElement().firstElement();
        layers.lastElement().clear();
        layers.lastElement().add(output);
    }

    /**
     * Updates inputVectors (input vectors of the learning matrix) with outputs of models from the last layer
     */
    private void updateInputVectors() {
        double[][] newInputVect = new double[maxLearningVectors][inputsNumber];
        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(inputVect[i], 0, newInputVect[i], 0, inputVect[i].length);
            int index = inputVect[i].length;
            for (Model model : layers.lastElement()) {
                newInputVect[i][index] = model.getOutput(inputVect[i]);
            }
        }
        inputVect = newInputVect;
    }


    /**
     * Returns hierarchical combination of surviving models
     *
     * @param input_vector Specify inputs to the model
     * @return output of the GAME hierarchical ensemble
     */
    public double getOutput(double[] input_vector) {
        return getOutputs(input_vector).lastElement();
    }

    private Vector<Double> getOutputs(double[] input_vector) {
        Vector<Double> outputs = new Vector<Double>();

        for (Vector<ModelEvolvable> layer : layers) {
            double[] input = new double[input_vector.length + outputs.size()];
            System.arraycopy(input_vector, 0, input, 0, input_vector.length);
            int index = input_vector.length;
            for (Double out : outputs) {
                input[index++] = out;
            }
            for (Model model : layer) {
                outputs.add(model.getOutput(input));
            }
        }
        return outputs;
    }

    public String toEquation(String[] inputEquation) {
        Vector<String> outputs = new Vector<String>();

        for (Vector<ModelEvolvable> layer : layers) {
            String[] input = new String[inputEquation.length + outputs.size()];
            System.arraycopy(inputEquation, 0, input, 0, inputEquation.length);
            int index = inputEquation.length;
            for (String out : outputs) {
                input[index++] = out;
            }
            for (Model model : layer) {
                outputs.add(model.toEquation(input));
            }
        }
        return outputs.lastElement();
    }

    public Class getConfigClass() {
        return GAMEEnsembleModelConfig.class;
    }
}