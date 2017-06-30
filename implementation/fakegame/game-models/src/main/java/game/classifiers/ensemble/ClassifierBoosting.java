package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierBoostingConfig;
import game.classifiers.Classifier;

/**
 * Implementation of Boosting for classifiers
 * Author: cernyjn
 */
public class ClassifierBoosting extends ClassifierWeighted {
    protected double[] clsWeights = new double[0];

    /**
     * Normalizes model weights
     */
    private void normalizeModelWeights() {
        double sum = 0;
        for (int i = 0; i < numClassifiers; i++) {
            sum += clsWeights[i];
        }
        //in case of infinity, remove weights other than infinity and make uniform distribution among infinity valued models
        if (sum == Double.POSITIVE_INFINITY) {
            sum = 0;
            for (int i = 0; i < numClassifiers; i++) {
                if (clsWeights[i] == Double.POSITIVE_INFINITY) clsWeights[i] = 1.0;
                else clsWeights[i] = 0.0;
                sum += clsWeights[i];
            }
        }
        //normalize
        for (int i = 0; i < numClassifiers; i++) {
            clsWeights[i] = clsWeights[i] / sum;
        }
    }

    protected void modifyWeights(double[] weights, int[] maxTarget, int clsIndex) {
        Classifier cls = ensClassifiers.get(clsIndex);
        int output;
        double errSum = 0;
        boolean[] wronglyClassified = new boolean[learning_vectors];
        for (int i = 0; i < learning_vectors; i++) {
            output = cls.getOutput(inputVect[i]);
            //increase instance weight if model output does not match target output
            if (output != maxTarget[i]) {
                errSum += weights[i];
                wronglyClassified[i] = true;
            }
        }
        //resize errsum from (0,1) to (0,0.5) interval
        errSum = errSum / 2;
        if (errSum == 0) {
            clsWeights[clsIndex] = Double.POSITIVE_INFINITY;
        } else {
            double beta = errSum / (1 - errSum);
            for (int i = 0; i < learning_vectors; i++) {
                if (!wronglyClassified[i]) weights[i] = weights[i] * beta;
            }
            clsWeights[clsIndex] = Math.log10(1.0 / beta);
        }
    }

    protected void initLearnArrays(double[] weights, int[] maxTarget) {
        super.initLearnArrays(weights, maxTarget);
        //check integrity of auxiliary data fields
        if (clsWeights.length != numClassifiers) {
            clsWeights = new double[numClassifiers];
        }
    }

    public void learn() {
        super.learn();
        normalizeModelWeights();
    }

    public void relearn() {
        super.relearn();
        normalizeModelWeights();
    }

    public void learn(int modelIndex) {
        super.learn(modelIndex);
        normalizeModelWeights();
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] clsOutput;
        double[] output = new double[outputs];
        for (int i = 0; i < numClassifiers; i++) {
            clsOutput = ensClassifiers.get(i).getOutputProbabilities(input_vector);
            for (int j = 0; j < clsOutput.length; j++) {
                output[j] += clsOutput[j] * clsWeights[i];
            }
        }
        return output;
    }

    public String[] getEquations(String[] inputEquation) {
        if (!learned) learn();
        String output[] = new String[outputs];
        Classifier cls;
        String[] equations;

        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            equations = cls.getEquations(inputEquation);
            for (int j = 0; j < equations.length; j++) {
                output[j] = clsWeights[i] + "*(" + equations[j] + ")" + (output[j] != "" ? "+" : "") + output[j];
            }
        }
        return output;
    }

    @Override
    public Class getConfigClass() {
        return ClassifierBoostingConfig.class;
    }
}
