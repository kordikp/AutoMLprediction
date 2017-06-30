package game.classifiers.ensemble;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.ClassifierCascadingConfig;
import game.classifiers.Classifier;

import java.util.Vector;

/**
 * Implementation of cascading for classifiers
 * Author: cernyjn
 */
public class ClassifierCascading extends ClassifierWeighted {
    private double threshold;

    /**
     * Modifies weights for next model depending on one of the situations below
     * instance is not correctly classified - largest weight
     * instance is correctly classified but with certainty below threshold - large weight
     * instance is correctly classified with certainty above threshold - low weight
     *
     * @param weights   Weights of instances.
     * @param maxTarget Array of indexes with maximum output probability.
     * @param clsIndex  Classifier index.
     */
    protected void modifyWeights(double[] weights, int[] maxTarget, int clsIndex) {
        Classifier cls = ensClassifiers.get(clsIndex);
        int outputIndex;
        double[] outputs;
        double deviation;
        for (int i = 0; i < learning_vectors; i++) {
            outputs = cls.getOutputProbabilities(inputVect[i]);
            outputIndex = maxIndex(outputs);
            deviation = Math.abs(target[i][maxTarget[i]] - outputs[maxTarget[i]]);
            //incorrectly classified instances
            if (outputIndex != maxTarget[i]) {
                weights[i] = weights[i] * deviation * 2;
            } else { //correctly classified instances
                if (outputs[outputIndex] < threshold) { //below threshold
                    weights[i] = weights[i] * deviation * 1.5;
                } else { //above threshold
                    weights[i] = weights[i] * deviation;
                }
            }
        }
    }

    public void init(ClassifierConfig cfg) {
        ClassifierCascadingConfig config = (ClassifierCascadingConfig) cfg;
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

    public ClassifierConfig getConfig() {
        ClassifierCascadingConfig cfg = (ClassifierCascadingConfig) super.getConfig();
        cfg.setThreshold(threshold);
        return cfg;
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

    @Override
    public Class getConfigClass() {
        return ClassifierCascadingConfig.class;
    }
}
