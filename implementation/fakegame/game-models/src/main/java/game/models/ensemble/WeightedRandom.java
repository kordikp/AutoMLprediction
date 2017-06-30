package game.models.ensemble;

import java.util.Random;

/**
 * Class to generate random weighted numbers
 * Author: cernyjn
 */
public class WeightedRandom {
    private Random rndGenerator;
    private double[] cumulativeProb;

    /**
     * 4 types of constructors for init with weights and without and for using own seed and using time as seed.
     */
    public WeightedRandom() {
        this(System.nanoTime());
    }

    public WeightedRandom(long seed) {
        rndGenerator = new Random(seed);
    }

    public WeightedRandom(double[] weights) {
        this(weights, System.nanoTime());
    }

    public WeightedRandom(double[] weights, long seed) {
        rndGenerator = new Random(seed);
        recomputeWeights(weights);
    }

    public void recomputeWeights(double[] weights) {
        cumulativeProb = new double[weights.length];
        double sum = 0;

        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
        }

        cumulativeProb[0] = weights[0] / sum;
        for (int i = 1; i < weights.length; i++) {
            cumulativeProb[i] = weights[i] / sum + cumulativeProb[i - 1];
        }
    }

    public void recomputeNormalizedWeights(double[] weights) {
        cumulativeProb = new double[weights.length];

        cumulativeProb[0] = weights[0];
        for (int i = 1; i < weights.length; i++) {
            cumulativeProb[i] = weights[i] + cumulativeProb[i - 1];
        }
    }

    public int randomWeightedNumber() {
        double rnd = rndGenerator.nextDouble();
        return findInCumulativeProb(rnd);
    }

    private int findInCumulativeProb(double num) {
        int min = 0;
        int max = cumulativeProb.length - 1;
        int ptr;
        while (true) {
            ptr = (max + min) / 2;
            if (num >= cumulativeProb[ptr]) {
                if (max - min == 1) return max;
                min = ptr;
            } else if (ptr != 0 && num < cumulativeProb[ptr - 1]) {
                max = ptr;
            } else return ptr;

        }
    }


}
