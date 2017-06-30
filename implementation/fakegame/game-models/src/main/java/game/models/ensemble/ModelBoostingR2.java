package game.models.ensemble;

import game.models.Model;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BoostingR2ModelConfig;

/**
 * Implementation of Boosting R2 algorithm
 * Author: cernyjn
 */
public class ModelBoostingR2 extends ModelBoosting {
    protected double modelsSpecialization;

    @Override
    protected void modifyWeights(double[] weights, int modelIndex) {
        double[] deviation = new double[learning_vectors];
        double maxDev = 0;
        double[] Lt = new double[learning_vectors];
        double LtSum = 0;
        double beta;
        Model model = ensembleModels.get(modelIndex);

        for (int i = 0; i < learning_vectors; i++) {
            deviation[i] = Math.abs(target[i] - model.getOutput(inputVect[i]));
            if (deviation[i] > maxDev) maxDev = deviation[i];
        }

        for (int i = 0; i < learning_vectors; i++) {
            Lt[i] = Math.pow(deviation[i] / maxDev, modelsSpecialization);
            LtSum += Lt[i] * weights[i];
        }
        beta = LtSum / (1 - LtSum);

        for (int i = 0; i < learning_vectors; i++) {
            weights[i] = weights[i] * Math.pow(beta, 1 - Lt[i]);
        }

        modelWeights[modelIndex] = Math.log(1 / beta);
    }

    public ModelConfig getConfig() {
        BoostingR2ModelConfig cfg = (BoostingR2ModelConfig) super.getConfig();
        cfg.setModelsSpecialization(modelsSpecialization);
        return cfg;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        BoostingR2ModelConfig ensCfg = (BoostingR2ModelConfig) cfg;
        modelsSpecialization = ensCfg.getModelsSpecialization();
    }

    @Override
    public Class getConfigClass() {
        return BoostingR2ModelConfig.class;
    }

}
