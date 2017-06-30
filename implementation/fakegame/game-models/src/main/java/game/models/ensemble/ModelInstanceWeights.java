package game.models.ensemble;

import game.models.ModelLearnable;


/**
 * Base class for models using instance weights
 * Author: cernyjn
 */
public abstract class ModelInstanceWeights extends ModelEnsembleBase {

    protected void prepareData(ModelLearnable LearnableModel, double[] weights) {
        LearnableModel.resetLearningData();
        normalizeWeights(weights);

        WeightedRandom rndGenerator = new WeightedRandom();
        rndGenerator.recomputeNormalizedWeights(weights);
        int rnd;

        for (int i = 0; i < LearnableModel.getMaxLearningVectors(); i++) {
            rnd = rndGenerator.randomWeightedNumber();
            //choose with repetition
            LearnableModel.storeLearningVector(this.inputVect[rnd], this.target[rnd]);
        }
    }

    protected void initLearnArrays(double[] weights) {
        //uniform distribution
        for (int i = 0; i < learning_vectors; i++)
            weights[i] = 1;
    }

    /**
     * Normalizes input array of weights.
     *
     * @param weights
     */
    protected void normalizeWeights(double[] weights) {
        double sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
        }

        for (int i = 0; i < weights.length; i++) {
            weights[i] = weights[i] / sum;
        }
    }

    /**
     * Modifies weights using output of model identified by modelIndex and computes weight of the current model.
     *
     * @param weights    Array of current weights.
     * @param modelIndex Index of model newly learned, so weights should be modified by it's output.
     */
    protected abstract void modifyWeights(double[] weights, int modelIndex);

    public void learn() {
        ModelLearnable LearnableModel;
        double[] weights = new double[learning_vectors];

        initLearnArrays(weights);

        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel, weights);
                    LearnableModel.learn();
                }
            }
            modifyWeights(weights, i);
        }
        learned = true;
    }

    public void relearn() {
        ModelLearnable LearnableModel;
        double[] weights = new double[learning_vectors];

        initLearnArrays(weights);

        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel, weights);
                relearnModel(LearnableModel);
            }
            modifyWeights(weights, i);
        }
        learned = true;
    }

    /**
     * Learns model from distribution gathered by getting output of all previous models.
     */
    public void learn(int modelIndex) {
        ModelLearnable LearnableModel;
        double[] weights = new double[learning_vectors];

        initLearnArrays(weights);

        //modify weights according to models before learned model
        for (int i = 0; i < modelIndex; i++) {
            //if model isnt learned learn it to prevent some inconsistencies by undefined or zero output
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                if (!LearnableModel.isLearned()) LearnableModel.learn();
            }
            modifyWeights(weights, i);
        }
        //learn model identified by modelIndex and all other models depending on it
        for (int i = modelIndex; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) ensembleModels.get(i);
                prepareData(LearnableModel, weights);
                relearnModel(LearnableModel);
            }
            modifyWeights(weights, i);
        }
        learned = true;
    }

}
