package game.models.ensemble;

import game.clusters.ArrayKMeans;
import game.models.ModelLearnable;
import game.utils.Utils;
import configuration.models.ModelConfig;
import configuration.models.ensemble.DivideModelConfig;

/**
 * Implementation of Dividing data based on area to models.
 * Author: cernyjn
 */
public class ModelDivide extends ModelEnsembleBase {

    private double[][] areaCenter;
    private double clusterSizeMultiplier;

    /**
     * Divides learning data to model according to which center are the data closest.
     */
    private void divideLearningData(int[][] clusterIndexes) {
        ModelLearnable learnableModel;
        for (int i = 0; i < clusterIndexes.length; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                learnableModel = (ModelLearnable) ensembleModels.get(i);
                learnableModel.setMaxLearningVectors(clusterIndexes[i].length);
                for (int j = 0; j < clusterIndexes[i].length; j++) {
                    learnableModel.storeLearningVector(inputVect[clusterIndexes[i][j]], target[clusterIndexes[i][j]]);
                }
            }
        }
    }

    private void createClusters() {
        double[][] clusterData = new double[learning_vectors][inputsNumber];
        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(inputVect[i], 0, clusterData[i], 0, inputsNumber);
        }

        ArrayKMeans kmeans = new ArrayKMeans(clusterData, modelsNumber);
        kmeans.setClusterSizeMultiplier(clusterSizeMultiplier);
        kmeans.run();
        areaCenter = kmeans.getCentroids();
        divideLearningData(kmeans.getMemberIndexes());
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        DivideModelConfig ensCfg = (DivideModelConfig) cfg;
        clusterSizeMultiplier = ensCfg.getClusterSizeMultiplier();
    }

    @Override
    public void learn() {
        createClusters();
        ModelLearnable LearnableModel;
        for (int i = 0; i < areaCenter.length; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    LearnableModel.learn();
                }
            }
        }
        learned = true;
    }

    @Override
    public void relearn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void learn(int modelIndex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private double combinedOutput(double[] input_vector) {
        int area = 1;
        if (area > areaCenter.length) area = areaCenter.length;
        //compute distances of input vector to all learning vectors
        double distance[] = new double[areaCenter.length];
        for (int i = 0; i < areaCenter.length; i++) {
            for (int j = 0; j < inputsNumber; j++) {
                distance[i] += Math.pow(input_vector[j] - areaCenter[i][j], 2);
            }
            distance[i] = Math.sqrt(distance[i]);
        }

        //init indexes array, sort will return sorted indexes according to distances
        int indexes[] = Utils.insertSort(distance, area);
        double[] modelInfluence = new double[areaCenter.length];
        double flatness = distance[indexes[0]];
        double influenceSum = 0;
        for (int i = 0; i < area; i++) {
            modelInfluence[indexes[i]] = Utils.gaussian(distance[indexes[i]], flatness) + 0.0001;
            if (i == 0) modelInfluence[indexes[i]] = modelInfluence[indexes[i]] + 1;
            influenceSum += modelInfluence[indexes[i]];
        }

        double output = 0;
        for (int i = 0; i < areaCenter.length; i++) {
            output += ensembleModels.get(i).getOutput(input_vector) * modelInfluence[i] / influenceSum;
        }
        return output;
    }

    private double singleModelOutput(double[] input_vector) {
        double distance;
        int modelIndex = 0;
        double minDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < areaCenter.length; i++) {
            distance = 0;
            for (int j = 0; j < inputsNumber; j++) {
                distance += Math.abs(input_vector[j] - areaCenter[i][j]);
            }
            if (distance < minDist) {
                minDist = distance;
                modelIndex = i;
            }
        }
        return ensembleModels.get(modelIndex).getOutput(input_vector);
    }

    public ModelConfig getConfig() {
        DivideModelConfig cfg = (DivideModelConfig) super.getConfig();
        cfg.setClusterSizeMultiplier(clusterSizeMultiplier);
        return cfg;
    }

    @Override
    public double getOutput(double[] input_vector) {
        return singleModelOutput(input_vector);
    }

    @Override
    public String toEquation(String[] inputEquation) {
        //todo: udelat na zaklade vzdalenosti od teziste -> pokud je nejbliz bodu Ti tak vystupem je model i atd..
        return null;
    }

    @Override
    public Class getConfigClass() {
        return DivideModelConfig.class;
    }
}
