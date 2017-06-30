/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.0
 * <p>
 * <p>Title: Continuous Ant Colony Optimization - Trainer</p>
 * <p>Description: trainer class for CACO</p>
 */

package game.trainers;

import game.trainers.ant.caco.Colony;
import game.trainers.gradient.Newton.Uncmin_methods;
import configuration.game.trainers.CACOConfig;

public class CACOTrainer extends Trainer implements Uncmin_methods {
    private static final long serialVersionUID = 1L;

    private int maxIterations;
    private int maxStagnation;
    private double searchRadius;
    private int directionsCount;
    private double radiusMultiplier;
    private int radiusGeneration;
    private double startingPheromone;
    private double minimumPheromone;
    private double addPheromone;
    private double evaporation;
    private double gradientWeight;
    private boolean debugOn;

    int cnt;
    private transient Colony colony;        // colony of ants used for computation

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        CACOConfig cf = (CACOConfig) cfg;

        maxIterations = cf.getMaxIterations();
        maxStagnation = cf.getMaxStagnation();
        searchRadius = cf.getSearchRadius();
        directionsCount = cf.getDirectionsCount();
        radiusMultiplier = cf.getRadiusMultiplier();
        radiusGeneration = cf.getRadiusGeneration();
        startingPheromone = cf.getStartingPheromone();
        minimumPheromone = cf.getMinimumPheromone();
        addPheromone = cf.getAddPheromone();
        evaporation = cf.getEvaporation();
        gradientWeight = cf.getGradientWeight();
        debugOn = cf.getDebugOn();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        colony = new Colony(this, coefficients, maxIterations,
                maxStagnation, searchRadius, directionsCount,
                radiusMultiplier, radiusGeneration, startingPheromone,
                minimumPheromone, addPheromone, evaporation,
                gradientWeight, debugOn);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        // if (cf.getGraphicsOn() && (window != null)) window.setVisible(true);
        colony.run();
        // if (cf.getGraphicsOn() && (window != null)) window.setVisible(false);
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "CACO: Continuous Ant Colony Optimization";
    }

    public double f_to_minimize(double[] x) {
        return getAndRecordError(x, 10, 100, true);
    }

    public void gradient(double[] x, double[] g) {
        unit.gradient(x, g);
    }

    public void hessian(double[] x, double[][] h) {
        unit.hessian(x, h);
    }

    public boolean allowedByDefault() {
        return false;
    }


    public Class getConfigClass() {
        return CACOConfig.class;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return true;
    }
}
