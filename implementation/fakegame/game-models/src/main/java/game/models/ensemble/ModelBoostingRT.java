package game.models.ensemble;

import game.models.Model;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BoostingRTModelConfig;

/**
 * Implementation of Boosting RT algorithm
 * Author: cernyjn
 */
public class ModelBoostingRT extends ModelBoosting {
    protected double threshold;

    @Override
    protected void modifyWeights(double[] weights, int modelIndex) {
        double[] deviation = new double[learning_vectors];
        double errSum = 0;
        double beta;

        Model model = ensembleModels.get(modelIndex);

        for (int i = 0; i < learning_vectors; i++) {
            deviation[i] = Math.abs((target[i] - model.getOutput(inputVect[i])) / target[i]);
            if (deviation[i] > threshold) errSum += weights[i];
        }

        beta = Math.pow(errSum, 2);

        for (int i = 0; i < learning_vectors; i++) {
            if (deviation[i] <= threshold) weights[i] = weights[i] * beta;
        }

        if (beta != 0) modelWeights[modelIndex] = Math.log(1 / beta);
        else modelWeights[modelIndex] = Double.POSITIVE_INFINITY;

    }

    public ModelConfig getConfig() {
        BoostingRTModelConfig cfg = (BoostingRTModelConfig) super.getConfig();
        cfg.setThreshold(threshold);
        return cfg;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        BoostingRTModelConfig ensCfg = (BoostingRTModelConfig) cfg;
        threshold = ensCfg.getThreshold();
    }

    @Override
    public Class getConfigClass() {
        return BoostingRTModelConfig.class;
    }

}
