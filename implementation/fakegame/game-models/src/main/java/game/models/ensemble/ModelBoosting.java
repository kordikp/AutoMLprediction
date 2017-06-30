package game.models.ensemble;


/**
 * Implementation of Boosting
 * Author: cernyjn
 */
public abstract class ModelBoosting extends ModelInstanceWeights {

    protected double[] modelWeights = new double[0];

    @Override
    protected void initLearnArrays(double[] weights) {
        super.initLearnArrays(weights);
        //check integrity of auxiliary data fields
        if (modelWeights.length != modelsNumber) {
            modelWeights = new double[modelsNumber];
        }
    }

    /**
     * Normalizes model weights
     */
    private void normalizeModelWeights() {
        double sum = 0;
        for (int i = 0; i < modelsNumber; i++) {
            sum += modelWeights[i];
        }
        //normalize
        if (sum == 0) {
            for (int i = 0; i < modelsNumber; i++) modelWeights[i] = 1 / modelsNumber;
        } else if (sum == Double.POSITIVE_INFINITY) {
            //if some model has infinity weight, use uniform weights among all models with infinity weight
            for (int i = 0; i < modelsNumber; i++) {
                if (modelWeights[i] == Double.POSITIVE_INFINITY) modelWeights[i] = 1;
                else modelWeights[i] = 0;
            }
            normalizeModelWeights();
        } else {
            for (int i = 0; i < modelsNumber; i++) modelWeights[i] = modelWeights[i] / sum;
        }
    }

    protected abstract void modifyWeights(double[] weights, int modelIndex);

    @Override
    public void learn() {
        super.learn();
        normalizeModelWeights();
    }

    @Override
    public void relearn() {
        super.relearn();
        normalizeModelWeights();
    }

    @Override
    public void learn(int modelIndex) {
        super.learn(modelIndex);
        normalizeModelWeights();
    }

    public double getOutput(double[] input_vector) {
        if (!learned) learn();
        double outValue = 0;
        for (int i = 0; i < modelsNumber; i++) {

            outValue += ensembleModels.get(i).getOutput(input_vector) * modelWeights[i];
        }
        return outValue;
    }

    public String toEquation(String[] inputEquation) {
        if (!learned) learn();
        String s = "";
        for (int i = 0; i < modelsNumber; i++) {
            s = modelWeights[i] + "*(" + ensembleModels.get(i).toEquation(inputEquation) + ")" + (s != "" && modelWeights[i - 1] >= 0 ? "+" : "") + s;
        }
        return s;
    }
}
