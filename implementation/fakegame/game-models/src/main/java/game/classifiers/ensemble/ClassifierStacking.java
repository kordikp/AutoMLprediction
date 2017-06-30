package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierStackingConfig;
import game.utils.MyRandom;
import game.classifiers.Classifier;

/**
 * Implementation of Stacking for classifiers. Metadata are derived from output classes of models.
 * Author: cernyjn
 */
public class ClassifierStacking extends ClassifierEnsembleBase {

    protected void prepareData(Classifier cls) {
        cls.resetLearningData();

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min = Math.min(cls.getMaxLearningVectors(), learning_vectors);
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            cls.storeLearningVector(inputVect[rnd], target[rnd]);
        }
    }

    protected void learnMetamodel(Classifier metaModel) {
        double[] metaData = new double[numClassifiers - 1];
        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min = Math.min(metaModel.getMaxLearningVectors(), learning_vectors);

        //gather metadata
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            for (int j = 0; j < numClassifiers - 1; j++) {
                metaData[j] = ensClassifiers.get(j).getOutput(inputVect[rnd]);
            }
            metaModel.storeLearningVector(metaData, target[rnd]);
        }
        //learn last model from metadata
        metaModel.learn();
    }

    public void learn() {
        Classifier cls;
        //learn models independently
        for (int i = 0; i < numClassifiers - 1; i++) {
            cls = ensClassifiers.get(i);
            //learn classifier if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls);
                cls.learn();
            }
        }
        //learn last model from metadata
        Classifier metaModel = ensClassifiers.get(numClassifiers - 1);
        if (!metaModel.isLearned()) {
            learnMetamodel(metaModel);
        }
        learned = true;
    }

    public void relearn() {
        Classifier cls;
        //learn models independently
        for (int i = 0; i < numClassifiers - 1; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls);
            cls.relearn();
        }
        //learn last model from metadata
        Classifier metaModel = ensClassifiers.get(numClassifiers - 1);
        learnMetamodel(metaModel);
        learned = true;
    }

    public void learn(int modelIndex) {
        Classifier cls;
        for (int i = 0; i < numClassifiers - 1; i++) {
            cls = ensClassifiers.get(i);
            //learn classifier if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls);
                cls.learn();
            } else if (i == modelIndex) { //relearn classifier identified by modelindex
                prepareData(cls);
                cls.relearn();
            }
        }

        Classifier metaModel = ensClassifiers.get(numClassifiers - 1);
        learnMetamodel(metaModel);
        learned = true;
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] metaData = new double[numClassifiers - 1];
        for (int i = 0; i < numClassifiers - 1; i++) {
            metaData[i] = ensClassifiers.get(i).getOutput(input_vector);
        }

        return ensClassifiers.get(numClassifiers - 1).getOutputProbabilities(metaData);
    }

    public String[] getEquations(String[] inputEquation) {
        return new String[0];
    }

    @Override
    public Class getConfigClass() {
        return ClassifierStackingConfig.class;
    }
}
