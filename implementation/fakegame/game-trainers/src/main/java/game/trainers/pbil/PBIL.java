package game.trainers.pbil;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * User: kovaro1
 * Date: 7.9.2009
 */
public class PBIL {
    // parameters of algorithm
    private int maxIterations;              // maximum of iterations
    private int maxStagnation;              // maximum of iterations without improvement

    private boolean debugOn;

    private DecimalFormat df;
    private int iteration;                  // current iteration
    private int stagnation;                 // iterations withou improvement
    private Uncmin_methods trainer;
    private int dimensions;                 // number of variables to optimize

    private double[] solution;              // solution vector
    private double min;
    private double max;

    private Random generator;

    private double[] bestVector;  // globaly best variables vector
    private double bestError;     // global error

    private int bitsPerVariable;
    private int populationSize;
    private double learnRate;
    private double negLearnRate;
    private double mutProb;
    private double mutShift;

    // constructor - stores parameters
    public PBIL(Uncmin_methods train, int dimensions, int maxIterations,
                int maxStagnation, double min, double max, int bitsPerVariable,
                int populationSize, double learnRate, double negLearnRate,
                double mutProb, double mutShift, boolean debugOn) {

        generator = new Random();

        this.trainer = train;
        this.dimensions = dimensions;
        this.debugOn = debugOn;

        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        this.min = min;
        this.max = max;
        this.bitsPerVariable = bitsPerVariable;
        this.populationSize = populationSize;
        this.learnRate = learnRate;
        this.negLearnRate = negLearnRate;
        this.mutProb = mutProb;
        this.mutShift = mutShift;
        this.populationSize = populationSize;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);

        solution = new double[dimensions];
        bestVector = new double[dimensions];
    }

    /**
     * main purpose for this is destoying window
     */
    public void destroy() {
        df = null;
        generator = null;
        solution = null;
    }

    // convert bit array to array of real numbers from interval <min, max>
    private double[] decode(boolean bits[]) {
        double result[] = new double[dimensions];
        double x;
        for (int i = 0; i < dimensions; i++) {
            x = (bits[i * bitsPerVariable] == false) ? 0.0 : 1.0;
            for (int j = 1; j < bitsPerVariable; j++) {
                x = x * 2.0 + ((bits[i * bitsPerVariable + j] == false) ? 0.0 : 1.0);
            }
            x /= Math.pow(2, bitsPerVariable) - 1;
            x = x * (max - min) + min;
            result[i] = x;
        }
        return result;
    }

    /**
     * main body of algorithm
     */
    public void run() {

        double error;
        bestError = Double.MAX_VALUE;
        iteration = 1;
        stagnation = 0;
        int totalBits = bitsPerVariable * dimensions;

        //System.out.println("Dimensions: " + dimensions);
        //System.out.println("total bits: " + totalBits);

        double[] probVec = new double[totalBits];
        // Random rand = new Random();
        Arrays.fill(probVec, 0.5);
        double bestCost = Double.POSITIVE_INFINITY;

        boolean[][] genes = new boolean[populationSize][totalBits];
        double[] costs = new double[populationSize];
        do {
            // Creates N genes
            for (int j = 0; j < populationSize; j++) {
                for (int k = 0; k < totalBits; k++) {
                    genes[j][k] = (generator.nextDouble() < probVec[k]);
                }
            }

            // Calculate costs
            for (int j = 0; j < populationSize; j++) {
                solution = decode(genes[j]);
                error = trainer.f_to_minimize(solution);

                // improvement?
                if (error < bestError) {
                    stagnation = 0;
                    bestError = error;
                    System.arraycopy(solution, 0, bestVector, 0, dimensions);
                    // System.out.println( Arrays.toString(solution) );
                }

                costs[j] = error; // costFunc.cost(toRealVec(genes[j], domains));
            }

            // Find min and max cost genes
            boolean[] minGene = null, maxGene = null;
            double minCost = Double.POSITIVE_INFINITY, maxCost = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < populationSize; j++) {
                double cost = costs[j];
                if (minCost > cost) {
                    minCost = cost;
                    minGene = genes[j];
                }
                if (maxCost < cost) {
                    maxCost = cost;
                    maxGene = genes[j];
                }
            }

            // Compare with the best cost gene
            if (bestCost > minCost) {
                bestCost = minCost;
                // bestGene = minGene;
            }

            // Update the probability vector with max and min cost genes
            for (int j = 0; j < totalBits; j++) {
                if (minGene[j] == maxGene[j]) {
                    probVec[j] = probVec[j] * (1d - learnRate) + (minGene[j] ? 1d : 0d) * learnRate;
                } else {
                    double learnRate2 = learnRate + negLearnRate;
                    probVec[j] = probVec[j] * (1d - learnRate2) + (minGene[j] ? 1d : 0d) * learnRate2;
                }
            }

            // Mutation
            for (int j = 0; j < totalBits; j++) {
                if (generator.nextDouble() < mutProb) {
                    probVec[j] = probVec[j] * (1d - mutShift) + (generator.nextBoolean() ? 1d : 0d) * mutShift;
                }
            }
            if (debugOn) System.out.println("Iteration: " + iteration + "; error: " + df.format(bestError));
        } while ((stagnation++ < maxStagnation) && (iteration++ < maxIterations));
    }

}
