package game.classifiers.ensemble;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.ClassifierDelegatingConfig;
import game.classifiers.Classifier;

/**
 * Implementation of delegating for classifiers
 * Author: cernyjn
 */
public class ClassifierDelegating extends ClassifierWeighted {
    private double threshold;

    /**
     * Set instance weight to 0 for instances that are correctly classified with certainty above threshold
     *
     * @param weights   Weights of instances.
     * @param maxTarget Array of indexes with maximum output probability.
     * @param clsIndex  Classifier index
     */
    protected void modifyWeights(double[] weights, int[] maxTarget, int clsIndex) {
        Classifier cls = ensClassifiers.get(clsIndex);
        int outputIndex;
        double[] outputs;
        for (int i = 0; i < learning_vectors; i++) {
            outputs = cls.getOutputProbabilities(inputVect[i]);
            outputIndex = maxIndex(outputs);
            if (maxTarget[i] == outputIndex && outputs[maxTarget[i]] >= threshold) {
                weights[i] = 0;
            }
        }
    }

    public void init(ClassifierConfig cfg) {
        ClassifierDelegatingConfig config = (ClassifierDelegatingConfig) cfg;
        threshold = config.getThreshold();

        super.init(cfg);
    }

    private double max(double[] array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) max = array[i];
        }
        return max;
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] output;
        double max;
        double soFarMax = Double.NEGATIVE_INFINITY;
        double[] soFarMaxOutput = new double[0];
        for (int i = 0; i < numClassifiers; i++) {
            output = ensClassifiers.get(i).getOutputProbabilities(input_vector);
            max = max(output);
            //return output of first model that exceeds threshold
            if (max >= threshold) {
                return output;
            } else if (max > soFarMax) { //save info about model with most certainty
                soFarMax = max;
                soFarMaxOutput = output;
            }
        }
        //if no model exceeds threshold return output of model with most certain result
        return soFarMaxOutput;
    }

    public String[] getEquations(String[] inputEquation) {
        if (!learned) learn();

        String[] equations;
        String[] output = new String[outputs];
        for (int i = 0; i < numClassifiers; i++) {
            equations = ensClassifiers.get(i).getEquations(inputEquation);
            for (int j = 0; j < outputs; j++) {
                output[j] = equations[j] + (output[j] != "" ? "," : "") + output[j];
            }
        }
        for (int i = 0; i < outputs; i++) {
            output[i] = "EQ={" + output[i] + "};return(firstTrue(EQ,EQ[i]>" + threshold + ",max(EQ)))";
        }
        return output;
    }

    public ClassifierConfig getConfig() {
        ClassifierDelegatingConfig cfg = (ClassifierDelegatingConfig) super.getConfig();
        cfg.setThreshold(threshold);
        return cfg;
    }

    @Override
    public Class getConfigClass() {
        return ClassifierDelegatingConfig.class;
    }
}
