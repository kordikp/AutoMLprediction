package game.classifiers;

import game.configuration.Configurable;
import configuration.classifiers.ClassifierConfig;

/**
 * Abstract class for commonly used methods in classifiers.
 * Author: cernyjn
 */
public abstract class ClassifierBase implements Classifier, Configurable {
    protected transient double[][] inputVect;
    protected transient double[][] target;
    protected int learning_vectors = 0;
    protected int inputs;
    protected int outputs;
    protected boolean learned = false;
    protected String name;
    protected int maxLearningVectors;

    public void init(ClassifierConfig cfg) {
        maxLearningVectors = cfg.getMaxLearningVectors();
        name = cfg.getName();
        //initialize variables
        learning_vectors = 0;
        learned = false;
    }

    public void setInputsNumber(int inputs) {
        this.inputs = inputs;
    }

    public void setOutputsNumber(int outs) {
        this.outputs = outs;
    }

    public int getInputsNumber() {
        return inputs;
    }

    public int getOutputsNumber() {
        return outputs;
    }

    public void storeLearningVector(double[] input, double[] output) {
        if (inputs != input.length || target == null || outputs != output.length) {
            inputs = input.length;
            outputs = output.length;
            inputVect = new double[maxLearningVectors][inputs];
            target = new double[maxLearningVectors][outputs];
        }

        System.arraycopy(output, 0, target[learning_vectors], 0, outputs);
        System.arraycopy(input, 0, inputVect[learning_vectors], 0, inputs);

        learning_vectors++;
    }

    public ClassifierConfig getConfig() {
        ClassifierConfig cfg = null;
        try {
            cfg = (ClassifierConfig) getConfigClass().newInstance();
            cfg.setClassRef(this.getClass());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public void setMaxLearningVectors(int maxVectors) {
        if (maxLearningVectors == -1) maxLearningVectors = maxVectors;
    }

    public int getMaxLearningVectors() {
        return maxLearningVectors;
    }

    public boolean isLearned() {
        return learned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toEquation(String[] inputEquation) {
        if (!learned) learn();
        String output = "";
        String[] equations = getEquations(inputEquation);

        for (int i = 0; i < outputs; i++) {
            //output name
            output += "\nClass " + i + "=";
            //equation for current output
            output += equations[i] + ";";
        }
        return output;
    }

    public void resetLearningData() {
        learning_vectors = 0;
    }

    public int getOutput(double[] input_vector) {
        if (!learned) learn();
        double[] output = getOutputProbabilities(input_vector);
        double max = output[0];
        int chosenAttribute = 0;
        //return index of attribute with highest probability
        for (int i = 1; i < output.length; i++) {
            if (output[i] > max) {
                max = output[i];
                chosenAttribute = i;
            }
        }
        return chosenAttribute;
    }

    public void deleteLearningVectors() {
        learning_vectors = 0;
        inputVect = new double[0][0];
        target = null;
    }


    public abstract Class getConfigClass();


    public double[][] getLearningInputVectors() {
        if (inputVect.length < 1) return new double[0][0];
        double[][] res = new double[inputVect[0].length][inputVect.length];
        for (int i = 0; i < inputVect.length; i++) {
            for (int j = 0; j < inputVect[0].length; j++) {
                res[j][i] = inputVect[i][j];
            }
        }
        return res;
    }

    public double[][] getLearningOutputVectors() {
        if (target.length < 1) return new double[0][0];
        double[][] res = new double[target[0].length][target.length];
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[0].length; j++) {
                res[j][i] = target[i][j];
            }
        }
        return res;
    }


    public void postLearnActions() {
        learned = true;
        deleteCurrentModelData();
    }

    public void deleteCurrentModelData() {
        learning_vectors = 0;
        inputVect = new double[0][0];
        target = null;
    }
}
