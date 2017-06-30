/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: Direct Ant Colony Optimization (DACO) - configuration</p>
 * <p>Description: class for DACO configuration</p>
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class DACOConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;

    private int maxIterations;          // maximum iterations of algorithm
    private int maxStagnation;          // maximum iterations without solution improvement
    // double minAcceptableError;  // minimal acceptable error
    private boolean debugOn;            // print debug info

    private int populationSize;         // number of ants in population
    private double evaporationFactor;   // lambda <0,1>
    private double min;                 // parameter minimum
    private double max;                 // parameter maximum

    private double gradientWeight;    // gradient impact

    /**
     * inicialises parametres to its default values
     */
    public DACOConfig() {
        maxIterations = 1000;
        // minAcceptableError  =     0;
        maxStagnation = 50;
        debugOn = false;

        populationSize = 40;
        evaporationFactor = 0.5;
        min = -10.0;
        max = 10.0;

        gradientWeight = 0.0;
    }


    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    // public double   getMinAcceptableError() { return minAcceptableError; }
    public boolean getDebugOn() {
        return debugOn;
    }

    public int getPopulationsize() {
        return populationSize;
    }

    public double getEvaporationFactor() {
        return evaporationFactor;
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
}
