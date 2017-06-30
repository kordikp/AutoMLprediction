package game.models.ensemble;

import game.models.Model;
import game.models.ModelLearnable;

import java.util.Random;

import configuration.models.ensemble.BaggingModelConfig;

/**
 * Implementation of Bagging
 * Author: cernyjn
 */
public class ModelBagging extends ModelEnsembleBase {

    private void prepareData(ModelLearnable LearnableModel) {
        LearnableModel.resetLearningData();

        Random rndGenerator = new Random(System.nanoTime());
        int rnd;

        for (int i = 0; i < LearnableModel.getMaxLearningVectors(); i++) {
            rnd = rndGenerator.nextInt(learning_vectors);
            //sample with replacement
            LearnableModel.storeLearningVector(inputVect[rnd], target[rnd]);
        }
    }

    public void learn() {
        ModelLearnable LearnableModel;
        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel);
                    LearnableModel.learn();
                }
            }
        }
        learned = true;
    }

    public void relearn() {
        ModelLearnable LearnableModel;
        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel);
                relearnModel(LearnableModel);
            }
        }
        learned = true;
    }

    /**
     * Return true if all models are learned.
     */
    protected boolean checkLearned() {
        if (learned == true) return true;
        ModelLearnable LearnableModel;
        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //if there is a non learned model return
                if (!LearnableModel.isLearned()) return false;
            }
        }
        return true;
    }

    public void learn(int modelIndex) {
        if (ensembleModels.get(modelIndex) instanceof ModelLearnable) {
            ModelLearnable LearnableModel = (ModelLearnable) ensembleModels.get(modelIndex);
            prepareData(LearnableModel);
            relearnModel(LearnableModel);
            learned = checkLearned();
        }
    }

    public double getOutput(double[] input_vector) {
        if (!learned) learn();
        double outValue = 0;
        for (Model model : ensembleModels) {
            outValue += model.getOutput(input_vector);
        }

        return outValue / modelsNumber;
    }

    public String toEquation(String[] inputEquation) {
        if (!learned) learn();
        String s = "";
        for (Model model : ensembleModels) {
            s = model.toEquation(inputEquation) + (s != "" ? "+" : "") + s;
        }
        return "(" + s + ")/" + modelsNumber;
    }

    @Override
    public Class getConfigClass() {
        return BaggingModelConfig.class;
    }

}
