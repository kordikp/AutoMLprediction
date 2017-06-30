/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: Continuous Ant Colony Optimization (CACO)- configuration</p>
 * <p>Description: class for CACO configuration</p>
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class CACOConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;

    private int directionsCount;        // number of directions heading from the nest
    private int maxIterations;          // maximum iterations of algorithm
    private int maxStagnation;          // maximum iterations without improvement
    // double  minAcceptableError = 0; // minimal acceptable error
    private double searchRadius;           // initial radius for random ant movement
    private boolean debugOn;                // print debug info
    // boolean graphicsOn;             // show visualisation

    private double radiusMultiplier;       // search radius decrease speed
    private int radiusGeneration;       // generations befora decrease
    private double startingPheromone;      // initial pheromone amount
    private double minimumPheromone;       // minimum pheromone amount
    private double addPheromone;           // pheromone amount to add
    private double evaporation;            // evaporation constant
    private double gradientWeight;     // gradient impact


    /**
     * inicialises parametres to its default values
     */
    public CACOConfig() {
        directionsCount = 20;
        maxIterations = 5000;
        maxStagnation = 500;
        // minAcceptableError = 0;
        searchRadius = 50.0;

        radiusMultiplier = 0.8;
        radiusGeneration = 30;
        startingPheromone = 0.1;
        minimumPheromone = 0.1;
        addPheromone = 0.3;
        evaporation = 0.3;
        gradientWeight = 0.0;

        debugOn = false;
        // graphicsOn = false;
        // window = new CACOWindow();
    }

    public int getDirectionsCount() {
        return directionsCount;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    // public double getMinAcceptableError() { return minAcceptableError; }
    public double getSearchRadius() {
        return searchRadius;
    }

    public boolean getDebugOn() {
        return debugOn;
    }
    // public boolean getGraphicsOn() { return graphicsOn; }
    // public CACOWindow getWindow() { return window; }

    public double getRadiusMultiplier() {
        return radiusMultiplier;
    }

    public int getRadiusGeneration() {
        return radiusGeneration;
    }

    public double getStartingPheromone() {
        return startingPheromone;
    }

    public double getMinimumPheromone() {
        return minimumPheromone;
    }

    public double getAddPheromone() {
        return addPheromone;
    }

    public double getEvaporation() {
        return evaporation;
    }

    public double getGradientWeight() {
        return gradientWeight;
    }

}
