/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Random Search - configuration</p>
 * <p>Description: class for Random Search configuration</p>
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class RandomConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;
    private int maxIterations;          // maximum iterations of algorithm
    private int maxStagnation;          // maximum iterations without improvement
    // double minAcceptableError;  // minimal acceptable error
    private boolean debugOn;            // print debug info

    private double min;                 // parameter minimum
    private double max;                 // parameter maximum
    private double gradientWeight;      // impact of a gradient information
    private int cycle;                  // # of iterations before randomization


    /**
     * inicialises parametres to its default values
     */
    public RandomConfig() {
        maxIterations = 2000;
        maxStagnation = 500;
        // minAcceptableError = 0;
        debugOn = false;

        min = -20.0;
        max = 20.0;
        gradientWeight = 0.0;
        cycle = 10;
    }


    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    // public double getMinAcceptableError() { return minAcceptableError; }
    public boolean getDebugOn() {
        return debugOn;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getGradientWeight() {
        return gradientWeight;
    }

    public int getCycle() {
        return cycle;
    }

}
