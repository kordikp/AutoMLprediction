/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Random Search</p>
 * <p>Description: class for Random Search</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 */

package game.trainers.random;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Random;

public class RandomSearch {

    // parameters of algorithm
    // double minAcceptableError;   // minimal acceptable error
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

    private double[] gradient;                 // solution gradient
    private int cycle;                      // # of iterations before randomization
    private double gradientWeight;             // x += gradientWeight * y, y from (-1, 1)

    /**
     * constructor - saves parameters
     *
     * @param train
     * @param dimensions
     * @param maxIterations
     * @param maxStagnation
     * @param min
     * @param max
     * @param gradientWeight
     * @param cycle
     * @param debugOn
     */
    public RandomSearch(Uncmin_methods train, int dimensions, int maxIterations,
                        int maxStagnation, double min, double max, double gradientWeight,
                        int cycle, boolean debugOn) {

        generator = new Random();

        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        // this.minAcceptableError = minAcceptableError;
        this.min = min;
        this.max = max;

        this.dimensions = dimensions;
        this.gradientWeight = gradientWeight;
        this.cycle = cycle;

        this.debugOn = debugOn;
        trainer = train;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);

        solution = new double[dimensions];
        bestVector = new double[dimensions];
        gradient = new double[dimensions];
    }

    /**
     * main purpose for this is destoying window
     */
    public void destroy() {
        df = null;
        generator = null;
        solution = null;
    }

    /**
     * main body of algorithm
     */
    public void run() {

        double error;
        bestError = Double.MAX_VALUE;
        iteration = 1;
        stagnation = 0;

        // random search with gradient search
        do {

            if ((iteration % cycle) == 1) {
                // random solution
                for (int i = 0; i < dimensions; i++) {
                    solution[i] = (max - min) * generator.nextDouble() + min;
                }
            } else {
                // improve solution using gradient
                trainer.gradient(solution, gradient);
                for (int i = 0; i < dimensions; i++) {
                    // solution[i] += gradientWeight * solution[i] * Math.atan(gradient[i])/halfPI;
                    solution[i] -= gradientWeight * gradient[i];
                }
            }

            error = trainer.f_to_minimize(solution);

            // improvement?
            if (error < bestError) {
                stagnation = 0;
                bestError = error;
                System.arraycopy(solution, 0, bestVector, 0, dimensions);
                // for (int i=0; i<dimensions; i++) bestVector[i] = solution[i];
            }

            if (debugOn) System.out.println("Iteration: " + iteration + "; error: " + df.format(bestError));

        } while ((stagnation++ < maxStagnation) && (iteration++ < maxIterations));
    }
}
