package game.classifiers.ensemble;

import game.classifiers.Classifier;
import game.models.ensemble.WeightedRandom;

/**
 * Abstract parent class for all classifiers using vector weights.
 * Boosting
 * Cascading
 * Delegating
 */
public abstract class ClassifierWeighted extends ClassifierEnsembleBase {

    protected void prepareData(Classifier cls, double[] weights) {
        cls.resetLearningData();
        normalizeWeights(weights);

        WeightedRandom rndGenerator = new WeightedRandom();
        rndGenerator.recomputeNormalizedWeights(weights);
        int rnd;

        for (int i = 0; i < cls.getMaxLearningVectors(); i++) {
            rnd = rndGenerator.randomWeightedNumber();
            //choose with repetition
            cls.storeLearningVector(inputVect[rnd], target[rnd]);
        }
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

    public static int maxIndex(double[] array) {
        double max = array[0];
        int maxIdx = 0;

        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    protected void initLearnArrays(double[] weights, int[] maxTarget) {
        //uniform distribution
        for (int i = 0; i < learning_vectors; i++)
            weights[i] = 1;
        //get maximum from outputs for learning vector
        for (int i = 0; i < learning_vectors; i++) {
            maxTarget[i] = maxIndex(target[i]);
        }
    }

    protected abstract void modifyWeights(double[] weights, int[] maxTarget, int clsIndex);

    public void learn() {
        Classifier cls;
        double[] weights = new double[learning_vectors];
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(weights, maxTarget);

        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            //learn classifier if its not already learned
            if (!cls.isLearned()) {
                prepareData(cls, weights);
                cls.learn();
            }
            modifyWeights(weights, maxTarget, i);
        }
        learned = true;
    }

    public void relearn() {
        Classifier cls;
        double[] weights = new double[learning_vectors];
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(weights, maxTarget);

        for (int i = 0; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls, weights);
            cls.relearn();
            modifyWeights(weights, maxTarget, i);
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        double[] weights = new double[learning_vectors];
        int[] maxTarget = new int[learning_vectors];
        initLearnArrays(weights, maxTarget);

        //modify weights according to models before learned model
        for (int i = 0; i < modelIndex; i++) {
            if (!ensClassifiers.get(i).isLearned()) ensClassifiers.get(i).learn();
            modifyWeights(weights, maxTarget, i);
        }
        //learn model identified by modelIndex and all other models depending on it
        Classifier cls;
        for (int i = modelIndex; i < numClassifiers; i++) {
            cls = ensClassifiers.get(i);
            prepareData(cls, weights);
            cls.relearn();
            modifyWeights(weights, maxTarget, i);
        }
        learned = true;
    }
}
