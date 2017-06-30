package game.models.ensemble;

import game.models.Model;
import game.models.ModelLearnable;
import game.utils.MyRandom;
import configuration.models.ensemble.CascadeGenModelConfig;

/**
 * Implementation of Cascade Generalization
 * Author: cernyjn
 */
public class ModelCascadeGen extends ModelEnsembleBase {

    private void prepareData(ModelLearnable learnableModel, double[][] modifiedData) {
        learnableModel.resetLearningData();

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min;
        //pass minimum from avaliable vectors and maxLearningVectors of classifierModel
        if (learnableModel.getMaxLearningVectors() > learning_vectors) min = learning_vectors;
        else min = learnableModel.getMaxLearningVectors();

        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            learnableModel.storeLearningVector(modifiedData[rnd], target[rnd]);
        }
    }

    /**
     * Creates new array, duplicates everything from previous array and adds new column coresponding to outputs of
     * last learned model identified by pos.
     *
     * @param data 2D array of so far obtained data
     * @param pos  position of last learned model
     * @return modified data
     */
    private double[][] addToData(double[][] data, int pos) {
        Model model = ensembleModels.get(pos);
        double[][] newModData = new double[learning_vectors][inputsNumber + pos + 1];

        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(data[i], 0, newModData[i], 0, inputsNumber + pos);
            newModData[i][inputsNumber + pos] = model.getOutput(data[i]);
        }
        return newModData;
    }

    public void learn() {
        ModelLearnable LearnableModel;
        double[][] modifiedData = inputVect;

        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel, modifiedData);
                    LearnableModel.learn();
                }
            }
            if (i != modelsNumber - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public void relearn() {
        ModelLearnable LearnableModel;
        double[][] modifiedData = inputVect;

        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel, modifiedData);
                LearnableModel.learn();
            }
            if (i != modelsNumber - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        ModelLearnable LearnableModel;
        double[][] modifiedData = inputVect;
        //gather outputs of previous models
        for (int i = 0; i < modelIndex; i++) {
            //if model isnt learned learn it to prevent some inconsistencies by undefined or zero output
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                if (!LearnableModel.isLearned()) LearnableModel.learn();
            }
            modifiedData = addToData(modifiedData, i);
        }
        //learn model at modelIndex and all models depending on it
        for (int i = modelIndex; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel, modifiedData);
                relearnModel(LearnableModel);
            }
            if (i != modelsNumber - 1) modifiedData = addToData(modifiedData, i);
        }
        learned = true;
    }

    public double getOutput(double[] input_vector) {
        if (!learned) learn();
        double[] modifiedInput = input_vector; // = new double[inputsNumber+modelsNumber-1];

        double[] newModInput;
        for (int i = 0; i < modelsNumber - 1; i++) {
            newModInput = new double[inputsNumber + i + 1];
            System.arraycopy(modifiedInput, 0, newModInput, 0, inputsNumber + i);
            newModInput[inputsNumber + i] = ensembleModels.get(i).getOutput(modifiedInput);
            modifiedInput = newModInput;
        }
        return ensembleModels.get(modelsNumber - 1).getOutput(modifiedInput);
    }

    /**
     * Last model has (global inputs + numModels-1) inputs.
     * Go over all of them and replace input markers from last to first.
     *
     * @param inputEquation
     * @return Array of equations for each output variable.
     */
    public String toEquation(String[] inputEquation) {
        String[] inputs = new String[inputEquation.length + modelsNumber - 1];
        System.arraycopy(inputEquation, 0, inputs, 0, inputEquation.length);
        for (int i = inputEquation.length; i < inputs.length; i++)
            inputs[i] = "#metaInput" + (i - inputEquation.length) + "#";

        String eq = ensembleModels.get(modelsNumber - 1).toEquation(inputs);
        String metaEq;
        String[] newInputs;
        for (int i = modelsNumber - 2; i >= 0; i--) {
            newInputs = new String[inputs.length - 1];
            System.arraycopy(inputs, 0, newInputs, 0, newInputs.length);
            inputs = newInputs;
            metaEq = ensembleModels.get(i).toEquation(inputs);
            eq = eq.replace("#metaInput" + i + "#", metaEq);
        }
        return eq;
    }

    @Override
    public Class getConfigClass() {
        return CascadeGenModelConfig.class;
    }
}
