/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Ant Colony Optimization (ACO*) - Trainer</p>
 * <p>Description: trainer class for ACO*</p>
 */

package game.trainers;

import game.trainers.ant.aco.Colony;
import game.trainers.gradient.Newton.Uncmin_methods;
import configuration.game.trainers.ACOConfig;

public class ACOTrainer extends Trainer implements Uncmin_methods {
    private static final long serialVersionUID = 1L;
    int cnt;

    private int maxIterations;
    private int maxStagnation;
    private boolean debugOn;
    private int populationSize;
    private double R, Q;
    private int replace;
    private boolean standardDeviation;
    private boolean forceDiversity;
    private double diversityLimit;
    private double gradientWeight;

    private transient Colony colony;        // colony of ants used for computation

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        ACOConfig cf = (ACOConfig) cfg;

        populationSize = cf.getPopulationSize();
        maxIterations = cf.getMaxIterations();
        maxStagnation = cf.getMaxStagnation();
        R = cf.getR();
        Q = cf.getQ();
        replace = cf.getReplace();
        standardDeviation = cf.getStandardDeviation();
        forceDiversity = cf.getForceDiversity();
        diversityLimit = cf.getDiversityLimit();
        gradientWeight = cf.getGradientWeight();
        debugOn = cf.getDebugOn();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        colony = new Colony(this, coefficients, populationSize,
                maxIterations, maxStagnation, Q, R,
                replace, standardDeviation, forceDiversity,
                diversityLimit, gradientWeight, debugOn);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        colony.run();
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     *
     * @return name of the algorithm
     */
    public String getMethodName() {
        return "ACO*: Ant Colony Optimization";
    }

    /**
     * returns error of vector x
     *
     * @param x
     * @return
     */
    public double f_to_minimize(double[] x) {
        return getAndRecordError(x, 10, 100, true);
    }

    /**
     * calculate gradient g in point x
     *
     * @param x position
     * @param g gradient
     */
    public void gradient(double[] x, double[] g) {
        unit.gradient(x, g);
    }

    /**
     * calculate hessian h in point x
     *
     * @param x position
     * @param h hessian
     */
    public void hessian(double[] x, double[][] h) {
        unit.hessian(x, h);
    }

    public boolean allowedByDefault() {
        return false;
    }


    public Class getConfigClass() {
        return ACOConfig.class;
    }

    public boolean isExecutableInParallelMode() {
        return true;
    }

}
