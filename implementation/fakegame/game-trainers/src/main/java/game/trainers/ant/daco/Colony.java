/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Direct Ant Colony Optimization (DACO) - colony</p>
 * <p>Description: class for DACO colony</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 * <p>
 * <p>Source: A Direct Application of Ant Colony Optimization to Function Optimization
 * Problem in Continuous Domain
 * Min Kong and Peng Tian
 * </p>
 **/

package game.trainers.ant.daco;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Random;

public class Colony {

    // parameters of algorithm
    // double minAcceptableError;      // minimal acceptable error
    private int maxIterations;              // maximum of iterations
    private int maxStagnation;              // maximum of iterations with same best error
    private boolean debugOn;

    private int populationSize;             // number of ants in population
    private double evaporationFactor;       // lambda <0,1>
    private double min;                     // parameter minimum
    private double max;                     // parameter maximum

    private double gradientWeight;          // gradient heuristic impact

    private DecimalFormat df;
    private int iteration = 1;              // current iteration
    private int stagnation = 0;             // iterations without improvement

    private Uncmin_methods trainer;
    private int dimensions;                 // number of variables to optimize

    private Random generator;

    private double[] means;
    private double[] deviations;
    private double[][] paths;

    private double[] gBestVector; // global best variables vector
    private double gBestError;    // global error
    // public double lBestVector[]; // local best variables vector
    // public double lBestError;    // local error

    /**
     * constructor - saves parameters
     *
     * @param train
     * @param dimensions
     * @param maxIterations
     * @param maxStagnation
     * @param debugOn
     * @param populationSize
     * @param evaporationFactor
     * @param min
     * @param max
     * @param gradientWeight
     */
    public Colony(Uncmin_methods train, int dimensions, int maxIterations,
                  int maxStagnation, boolean debugOn, int populationSize,
                  double evaporationFactor, double min, double max, double gradientWeight) {

        generator = new Random();

        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        // this.minAcceptableError = minAcceptableError;

        this.populationSize = populationSize;
        this.evaporationFactor = evaporationFactor;
        this.min = min;
        this.max = max;

        this.gradientWeight = gradientWeight;

        this.dimensions = dimensions;
        this.debugOn = debugOn;
        trainer = train;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(6);
        df.setMinimumFractionDigits(6);

        means = new double[dimensions];
        deviations = new double[dimensions];

        paths = new double[populationSize][dimensions];

        gBestVector = new double[dimensions];
        // lBestVector = new double[dimensions];
    }

    /**
     * main purpose for this is destoying window
     */
    public void destroy() {
        df = null;
        generator = null;
    }

    /**
     * directions
     */
    void colonyDump() {
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < dimensions; j++) {
                System.out.print(df.format(paths[i][j]) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * main body of algorithm
     */
    public void run() {

        gBestError = Double.MAX_VALUE;
        // lBestError = Double.MAX_VALUE;

        //initialize pheromone
        for (int i = 0; i < dimensions; i++) {
            means[i] = (generator.nextDouble() * (max - min)) + min;
            deviations[i] = (max - min) / 2;
        }


        stagnation = 0;
        iteration = 1;

        // main cycle
        do {
            // lBestError = Double.MAX_VALUE;

            // for all ants generate solution
            for (int ant = 0; ant < populationSize; ant++) {
                // double[] newpos   = new double[dimensions];

                // generate solution
                for (int d = 0; d < dimensions; d++) {
                    paths[ant][d] = (generator.nextGaussian() * deviations[d]) + means[d];
                }

                // improve solution using gradient
                if (gradientWeight != 0.0) {
                    double[] gradient = new double[dimensions];
                    trainer.gradient(paths[ant], gradient);
                    for (int d = 0; d < dimensions; d++) {
                        paths[ant][d] = paths[ant][d] - gradientWeight * gradient[d];
                    }
                }

                // get solution error (& update best solution
                double error = trainer.f_to_minimize(paths[ant]);
                /* if (error < lBestError) {
                    lBestError = error;
                    for (int i=0;i<dimensions;i++) lBestVector[i] = paths[ant][i];
                } */
                if (error < gBestError) {
                    stagnation = 0;
                    gBestError = error;
                    System.arraycopy(paths[ant], 0, gBestVector, 0, dimensions);
                    // for (int i=0;i<dimensions;i++) gBestVector[i] = paths[ant][i];
                    //System.out.println("Best found! Iteration: " + iteration + "; best error: " + df.format(gBestError));
                }
            }

            // update pheromone
            for (int i = 0; i < dimensions; i++) {
                deviations[i] = (1 - evaporationFactor) * deviations[i];
                deviations[i] = deviations[i] + evaporationFactor * Math.abs(gBestVector[i] - means[i]);
                means[i] = (1 - evaporationFactor) * means[i];
                means[i] = means[i] + evaporationFactor * gBestVector[i];
            }

            if (debugOn) System.out.println("Iteration: " + iteration + "; best error: " + df.format(gBestError));
            if (debugOn) colonyDump();

        } while ((stagnation++ < maxStagnation) && (iteration++ < maxIterations));
        //} while ((iteration++ < maxIterations) && (gBestError > minAcceptableError));

    }

}
