/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: API - Trainer</p>
 * <p>Description: trainer class for API</p>
 */

package game.trainers;

import game.trainers.ant.api.Nest;
import game.trainers.gradient.Newton.Uncmin_methods;
import configuration.game.trainers.APIConfig;

public class APITrainer extends Trainer implements Uncmin_methods {
    private static final long serialVersionUID = 1L;

    private int populationSize;
    private int huntingSites;
    private int moveGeneration;
    private int starvation;
    private int maxIterations;
    private double minAcceptableError;
    private boolean debugOn;
    private double gradientWeight;

    int cnt;
    private Nest nest;          // colony of ants used for computation

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        APIConfig cf = (APIConfig) cfg;

        populationSize = cf.getPopulationSize();
        huntingSites = cf.getHuntingSites();
        moveGeneration = cf.getMoveGeneration();
        starvation = cf.getStarvation();
        maxIterations = cf.getMaxIterations();
        minAcceptableError = cf.getMinAcceptableError();
        debugOn = cf.getDebugOn();
        gradientWeight = cf.getGradientWeight();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        nest = new Nest((Uncmin_methods) this, coefficients, populationSize,
                huntingSites, moveGeneration, starvation, maxIterations,
                minAcceptableError, debugOn, gradientWeight);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        nest.run();
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "API";
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
        return APIConfig.class;
    }

    public boolean isExecutableInParallelMode() {
        return true;
    }
}
