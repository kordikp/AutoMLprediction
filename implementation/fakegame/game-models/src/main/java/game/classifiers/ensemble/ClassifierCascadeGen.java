package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierCascadeGenConfig;
import game.utils.MyRandom;
import game.classifiers.Classifier;

/**
 * Implementation of Cascade generalization for classifiers
 * Author: cernyjn
 */
public class ClassifierCascadeGen extends ClassifierEnsembleBase {

    protected void prepareData(Classifier cls, double[][] modifiedData) {
        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int min = Math.min(learning_vectors, cls.getMaxLearningVectors());

        int rnd;
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            cls.storeLearningVector(modifiedData[rnd], target[rnd]);
        }
    }

    /**
     * Creates new array, duplicates everything from previous array and adds new column coresponding to outputs of
     * last learned model identified by pos.
     *
     * @param data 2D array of so far obtained data
     * @param pos  position of last learned model
     */
    protected double[][] addToData(double[][] data, int pos) {
        Classifier cls = ensClassifiers.get(pos);
        double[][] newModData = new double[learning_vectors][inputs + pos + 1];

        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(data[i], 0, newModData[i], 0, inputs + pos);
            newModData[i][inputs + pos] = cls.getOutput(data[i]);
        }
        return newModData;
    }

    public void learn() {
        Classifier cls;
        double[][] modifiedData = inputVect;

        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            //learn model if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls, modifiedData);
                cls.learn();
            }
            if (i != numClassifiers - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public void relearn() {
        Classifier cls;
        double[][] modifiedData = inputVect;

        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls, modifiedData);
            cls.relearn();
            if (i != numClassifiers - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        Classifier cls;
        double[][] modifiedData = inputVect;
        //gather outputs of previous models
        for (int i = 0; i < modelIndex; i++) {
            if (!ensClassifiers.get(i).isLearned()) ensClassifiers.get(i).learn();
            modifiedData = addToData(modifiedData, i);
        }
        //learn model at modelIndex and all models depending on it
        for (int i = modelIndex; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls, modifiedData);
            cls.relearn();
            if (i != numClassifiers - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] modifiedInput = input_vector; // = new double[inputs+modelsNumber-1];

        double[] newModInput;
        for (int i = 0; i < numClassifiers - 1; i++) {
            newModInput = new double[inputs + i + 1];
            System.arraycopy(modifiedInput, 0, newModInput, 0, inputs + i);
            newModInput[inputs + i] = ensClassifiers.get(i).getOutput(modifiedInput);
            modifiedInput = newModInput;
        }
        return ensClassifiers.get(numClassifiers - 1).getOutputProbabilities(modifiedInput);
    }

    /**
     * Last model has (global inputs + numClassifiers-1) inputs.
     * Go over all of them and replace input markers from last to first.
     *
     * @return Array of equations for each output variable.
     */
    public String[] getEquations(String[] inputEquation) {
        return new String[0];
    }

    @Override
    public Class getConfigClass() {
        return ClassifierCascadeGenConfig.class;
    }
}
