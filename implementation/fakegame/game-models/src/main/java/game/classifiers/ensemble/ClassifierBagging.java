package game.classifiers.ensemble;

import game.classifiers.Classifier;

import java.util.Random;

import configuration.classifiers.ensemble.ClassifierBaggingConfig;

/**
 * Implementation of Bagging for classifiers
 * Author: cernyjn
 */
public class ClassifierBagging extends ClassifierEnsembleBase {
    /**
     * Pass data to classifier cls from inputVect chosen by selection with repetition.
     *
     * @param cls
     */
    private void prepareData(Classifier cls) {
        cls.resetLearningData();

        Random rndGenerator = new Random(System.nanoTime());
        int min = Math.min(learning_vectors, cls.getMaxLearningVectors());
        int rnd;
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.nextInt(learning_vectors);
            //choose with repetition
            cls.storeLearningVector(inputVect[rnd], target[rnd]);
        }
    }

    public void learn() {
        Classifier cls;
        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            //learn model if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls);
                cls.learn();
            }
        }
        learned = true;
    }

    public void relearn() {
        Classifier cls;
        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls);
            cls.relearn();
        }
        learned = true;
    }

    /**
     * Return true if all models are learned
     */
    protected boolean checkLearned() {
        if (learned) return true;
        for (int i = 0; i < numClassifiers; i++) {
            if (!ensClassifiers.get(i).isLearned()) return false;
        }
        return true;
    }

    public void learn(int modelIndex) {
        Classifier cls = ensClassifiers.get(modelIndex);
        prepareData(cls);
        cls.relearn();
        learned = checkLearned();
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] clsOutput;
        double[] output = new double[outputs];
        for (int i = 0; i < numClassifiers; i++) {
            clsOutput = ensClassifiers.get(i).getOutputProbabilities(input_vector);
            for (int j = 0; j < clsOutput.length; j++) {
                output[j] += clsOutput[j] / numClassifiers;
            }
        }
        return output;
    }

    public String[] getEquations(String[] inputEquation) {
        if (!learned) learn();
        String output[] = new String[outputs];
        Classifier cls;
        String[] equations;
        for (int i = 0; i < outputs; i++) output[i] = "";
        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            equations = cls.getEquations(inputEquation);
            for (int j = 0; j < equations.length; j++) {
                output[j] = equations[j] + (output[j] != "" ? "+" : "") + output[j];
            }
        }

        for (int i = 0; i < outputs; i++) {
            if (output[i] != "") output[i] = "(" + output[i] + ")/" + numClassifiers;
        }
        return output;
    }

    @Override
    public Class getConfigClass() {
        return ClassifierBaggingConfig.class;
    }
}
