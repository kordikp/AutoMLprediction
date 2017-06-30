/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: API - configuration</p>
 * <p>Description: class for API configuration</p>
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class APIConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;

    private int populationSize; // number of ants in colony
    private int huntingSites;   // number of hunting sites for one ant
    private int moveGeneration; // number of generations before moving the nest
    private int starvation;     // number of iterations without solution before removing hunting site
    private int maxIterations;  // maximum iterations of algorithm
    private double minAcceptableError = 0;  // minimal acceptable error

    private double gradientWeight = 0.0;    // gradient impact

    private boolean debugOn;    // print debug info


    /**
     * inicialises parametres to its default values
     */
    public APIConfig() {
        starvation = 5;
        moveGeneration = 30;
        huntingSites = 3;
        populationSize = 20;
        maxIterations = 300;
        minAcceptableError = 0;
        gradientWeight = 0.0;
        debugOn = false;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getHuntingSites() {
        return huntingSites;
    }

    public int getMoveGeneration() {
        return moveGeneration;
    }

    public int getStarvation() {
        return starvation;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public double getMinAcceptableError() {
        return minAcceptableError;
    }

    public boolean getDebugOn() {
        return debugOn;
    }

    public double getGradientWeight() {
        return gradientWeight;
    }

}
