package game.models.ensemble;

import game.models.ModelLearnable;
import game.utils.MyRandom;
import configuration.models.ensemble.StackingModelConfig;

/**
 * Implementation of Stacking
 * Author: cernyjn
 */
public class ModelStacking extends ModelEnsembleBase {

    private void prepareData(ModelLearnable LearnableModel) {
        LearnableModel.resetLearningData();

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min;
        //pass minimum from avaliable vectors and maxLearningVectors of classifierModel
        if (LearnableModel.getMaxLearningVectors() > learning_vectors) min = learning_vectors;
        else min = LearnableModel.getMaxLearningVectors();

        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            LearnableModel.storeLearningVector(inputVect[rnd], target[rnd]);
        }
    }

    private void learnMetamodel(ModelLearnable metaModel) {
        metaModel.resetLearningData();

        double[] metaData = new double[modelsNumber - 1];
        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min;
        if (metaModel.getMaxLearningVectors() > learning_vectors) min = learning_vectors;
        else min = metaModel.getMaxLearningVectors();

        //gather metadata
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            for (int j = 0; j < modelsNumber - 1; j++) {
                metaData[j] = ensembleModels.get(j).getOutput(inputVect[rnd]);
            }
            metaModel.storeLearningVector(metaData, target[rnd]);
        }
        //learn last model from metadata
        metaModel.learn();
    }

    public void learn() {
        ModelLearnable LearnableModel;
        //learn models independently
        for (int i = 0; i < modelsNumber - 1; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel);
                    LearnableModel.learn();
                }
            }
        }
        //learn last model from metadata
        if (ensembleModels.get(modelsNumber - 1) instanceof ModelLearnable) {
            ModelLearnable metaModel = (ModelLearnable) ensembleModels.get(modelsNumber - 1);
            if (!metaModel.isLearned()) {
                learnMetamodel(metaModel);
            }
        }
        learned = true;
    }

    public void relearn() {
        ModelLearnable LearnableModel;
        for (int i = 0; i < modelsNumber - 1; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel);
                relearnModel(LearnableModel);
            }
        }
        //learn last model from metadata
        if (ensembleModels.get(modelsNumber - 1) instanceof ModelLearnable) {
            ModelLearnable metaModel = (ModelLearnable) ensembleModels.get(modelsNumber - 1);
            learnMetamodel(metaModel);
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        ModelLearnable LearnableModel;
        for (int i = 0; i < modelsNumber - 1; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel);
                    LearnableModel.learn();
                } else if (i == modelIndex) { //relearn model identified by modelindex
                    prepareData(LearnableModel);
                    relearnModel(LearnableModel);
                }
            }
        }
        ModelLearnable metaModel = (ModelLearnable) ensembleModels.get(modelsNumber - 1);
        learnMetamodel(metaModel);
        learned = true;
    }


    public double getOutput(double[] input_vector) {
        if (!learned) learn();
        double[] metaData = new double[modelsNumber - 1];
        for (int i = 0; i < modelsNumber - 1; i++) {
            metaData[i] = ensembleModels.get(i).getOutput(input_vector);
        }
        return ensembleModels.get(modelsNumber - 1).getOutput(metaData);
    }

    public String toEquation(String[] inputEquation) {
        String[] inputs = new String[modelsNumber - 1];
        for (int i = 0; i < modelsNumber - 1; i++) inputs[i] = "#metaInput" + i + "#";

        String eq = ensembleModels.get(modelsNumber - 1).toEquation(inputs);
        String metaEq;
        for (int i = modelsNumber - 2; i >= 0; i--) {
            metaEq = ensembleModels.get(i).toEquation(inputEquation);
            eq = eq.replace("#metaInput" + i + "#", metaEq);
        }
        return eq;
    }


    @Override
    public Class getConfigClass() {
        return StackingModelConfig.class;
    }
}
