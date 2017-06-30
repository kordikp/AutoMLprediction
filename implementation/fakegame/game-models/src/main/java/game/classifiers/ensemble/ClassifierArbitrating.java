package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierArbitratingConfig;
import game.classifiers.Classifier;
import game.utils.MyRandom;

/**
 * Implementation of Arbitrating for classifiers
 * Classifier array has structure of:
 * classifier i (i even, starts at 0) - classifier
 * classifier i+1 - referee for classifier i
 * Author: cernyjn
 */
public class ClassifierArbitrating extends ClassifierEnsembleBase {

    protected void prepareData(Classifier cls) {
        cls.resetLearningData();

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min = Math.min(learning_vectors, cls.getMaxLearningVectors());
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            cls.storeLearningVector(inputVect[rnd], target[rnd]);
        }
    }

    protected void prepareMetaData(int refereeIndex, int[] maxTarget) {
        Classifier referee = ensClassifiers.get(refereeIndex);

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min = Math.min(learning_vectors, referee.getMaxLearningVectors());
        double[] outputs;
        double[] output = new double[2];
        int highestProbOutputIndex;
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            outputs = ensClassifiers.get(refereeIndex - 1).getOutputProbabilities(inputVect[rnd]);
            highestProbOutputIndex = maxIndex(outputs);
            //new output is set to probability determined by current classifier if its classified correctly,
            //otherwise it is left to 0
            if (maxTarget[rnd] == highestProbOutputIndex) {
                output[0] = outputs[highestProbOutputIndex];
                output[1] = 0;
            } else { //wrong classifications
                output[0] = 0;
                output[1] = 1;
            }
            referee.storeLearningVector(inputVect[rnd], output);
        }
    }

    protected void initLearnArrays(int[] maxTarget) {
        //get maximum from outputs for learning vector
        for (int i = 0; i < learning_vectors; i++) {
            maxTarget[i] = maxIndex(target[i]);
        }
    }

    private int maxIndex(double[] array) {
        double max = array[0];
        int maxIdx = 0;

        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    public void learn() {
        Classifier cls;
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(maxTarget);

        //learn models independently
        for (int i = 0; i < numClassifiers; i = i + 2) {
            cls = ensClassifiers.get(i);
            //learn classifier if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls);
                cls.learn();
                //learn coresponding referee
                if (i + 1 < numClassifiers) {
                    prepareMetaData(i + 1, maxTarget);
                    ensClassifiers.get(i + 1).learn();
                }
            }
        }
        learned = true;
    }

    public void relearn() {
        Classifier cls;
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(maxTarget);

        //learn models independently
        for (int i = 0; i < numClassifiers; i = i + 2) {
            cls = ensClassifiers.get(i);
            prepareData(cls);
            cls.learn();
            //learn coresponding referee
            if (i + 1 < numClassifiers) {
                prepareMetaData(i + 1, maxTarget);
                ensClassifiers.get(i + 1).learn();
            }
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        Classifier cls;
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(maxTarget);

        if (modelIndex % 2 == 0) {
            cls = ensClassifiers.get(modelIndex);
            prepareData(cls);
            cls.learn();
            //learn coresponding referee
            if (modelIndex + 1 < numClassifiers) {
                prepareMetaData(modelIndex + 1, maxTarget);
                ensClassifiers.get(modelIndex + 1).learn();
            }
        } else {
            prepareMetaData(modelIndex, maxTarget);
            ensClassifiers.get(modelIndex).learn();
        }
        learned = checkLearned();
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

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] output;
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex = 1;
        //get outputs from all referees and choose the one with highest output
        for (int i = 1; i < numClassifiers; i = i + 2) {
            output = ensClassifiers.get(i).getOutputProbabilities(input_vector);
            if (output[0] > max) {
                max = output[0];
                maxIndex = i;
            }
        }
        return ensClassifiers.get(maxIndex - 1).getOutputProbabilities(input_vector);
    }

    public String[] getEquations(String[] inputEquation) {
        if (!learned) learn();
        String[] equations;
        String[] output = new String[outputs / 2];
        String[] refOutput = new String[outputs / 2];
        for (int i = 1; i < numClassifiers; i = i + 2) {
            equations = ensClassifiers.get(i - 1).getEquations(inputEquation);
            for (int j = 0; j < outputs; j++) {
                output[j] = equations[j] + (output[j] != "" ? "," : "") + output[j];
            }
            //referee classifiers
            equations = ensClassifiers.get(i).getEquations(inputEquation);
            refOutput[i] = equations[0] + (refOutput[i] != "" ? "," : "") + refOutput[i];
        }

        for (int i = 0; i < outputs; i++) {
            output[i] = "EQ={" + output[i] + "};REFEQ={" + refOutput[i] + "};return(EQ[max(REFEQ)])";
        }
        return output;
    }

    @Override
    public Class getConfigClass() {
        return ClassifierArbitratingConfig.class;
    }
}
