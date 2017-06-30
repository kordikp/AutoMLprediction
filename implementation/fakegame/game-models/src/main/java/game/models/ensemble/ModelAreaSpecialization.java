package game.models.ensemble;

import game.models.Model;
import game.utils.Utils;
import configuration.models.ModelConfig;
import configuration.models.ensemble.AreaSpecializationModelConfig;

/**
 * Implementation of Area specialization
 * Author: cernyjn
 */
public class ModelAreaSpecialization extends ModelInstanceWeights {

    private int area;
    private double modelsSpecialization;

    private double[][] savedVectors;
    private double[] savedVectorsOutput;

    @Override
    protected void modifyWeights(double[] weights, int modelIndex) {
        double deviation;
        Model model = ensembleModels.get(modelIndex);

        for (int i = 0; i < learning_vectors; i++) {
            deviation = Math.abs(target[i] - model.getOutput(inputVect[i]));
            weights[i] = weights[i] * Math.pow(deviation, modelsSpecialization);
        }
    }

    /**
     * @param input_vector Input vector.
     * @param target_value Correct response to input vector.
     * @return Returns index of model with best response on input vector.
     */
    private int getBestModel(double[] input_vector, double target_value) {
        int bestModel = 0;
        double closestValue = Math.abs(ensembleModels.get(0).getOutput(input_vector) - target_value);
        double responseError;
        for (int i = 1; i < modelsNumber; i++) {
            responseError = Math.abs(ensembleModels.get(i).getOutput(input_vector) - target_value);
            if (responseError < closestValue) {
                closestValue = responseError;
                bestModel = i;
            }
        }
        return bestModel;
    }

    /**
     * Saves learning vectors to internal use for determining which model to use. In future can be replaced with less
     * memory demanding representation
     */
    private void saveLearningVectors() {
        savedVectors = new double[learning_vectors][inputsNumber];
        savedVectorsOutput = new double[learning_vectors];
        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(inputVect[i], 0, savedVectors[i], 0, inputsNumber);
        }
        System.arraycopy(target, 0, savedVectorsOutput, 0, learning_vectors);
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        AreaSpecializationModelConfig ensCfg = (AreaSpecializationModelConfig) cfg;
        area = ensCfg.getArea();
        modelsSpecialization = ensCfg.getModelsSpecialization();
    }

    @Override
    public void learn() {
        super.learn();
        saveLearningVectors();
    }

    @Override
    public void relearn() {
        super.relearn();
        saveLearningVectors();
    }

    @Override
    public void learn(int modelIndex) {
        super.learn(modelIndex);
        saveLearningVectors();
    }

    @Override
    public double getOutput(double[] input_vector) {
        //fix possibility when model gets less learning vectors than area, but do not overwrite model settings.
        int workingArea;
        if (area > savedVectors.length) workingArea = savedVectors.length;
        else workingArea = area;
        //compute distances of input vector to all learning vectors
        double distance[] = new double[savedVectors.length];
        for (int i = 0; i < savedVectors.length; i++) {
            for (int j = 0; j < inputsNumber; j++) {
                distance[i] += Math.pow(input_vector[j] - savedVectors[i][j], 2);
            }
            distance[i] = Math.sqrt(distance[i]);
        }
        //init indexes array, sort will return sorted indexes according to distances
        int indexes[] = Utils.insertSort(distance, workingArea);

        //flatness defined by farest learning vector from area
        double flatness = distance[indexes[indexes.length - 1]];
        //find best model for specific learning vector nearby and compute it's influence on input_vector determined by distance
        double[] modelInfluence = new double[modelsNumber];
        int bestModel;
        double distanceFromBestModel;
        for (int i = 0; i < workingArea; i++) {
            bestModel = getBestModel(savedVectors[indexes[i]], savedVectorsOutput[indexes[i]]);
            distanceFromBestModel = savedVectorsOutput[indexes[i]] - ensembleModels.get(bestModel).getOutput(input_vector);
            modelInfluence[bestModel] += Utils.gaussian(distanceFromBestModel, flatness) + 0.0001;
        }

        //compute sum of influence for normalization
        double influenceSum = 0;
        for (int i = 0; i < modelsNumber; i++) {
            influenceSum += modelInfluence[i];
        }

        double output = 0;
        for (int i = 0; i < modelsNumber; i++) {
            if (modelInfluence[i] == 0) continue;
            output += ensembleModels.get(i).getOutput(input_vector) * modelInfluence[i] / influenceSum;
        }
        return output;
    }

    public ModelConfig getConfig() {
        AreaSpecializationModelConfig cfg = (AreaSpecializationModelConfig) super.getConfig();
        cfg.setArea(area);
        cfg.setModelsSpecialization(modelsSpecialization);
        return cfg;
    }

    @Override
    public String toEquation(String[] inputEquation) {
        //todo: intervaly v kterych plati model x, intervaly v kterych plati model y...
        return null;
    }

    @Override
    public Class getConfigClass() {
        return AreaSpecializationModelConfig.class;
    }
}
