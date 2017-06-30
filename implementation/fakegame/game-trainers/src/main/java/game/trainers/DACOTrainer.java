/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Direct Ant Colony Optimization with gradient (DACO) - Trainer</p>
 * <p>Description: trainer class for DACO</p>
 */

package game.trainers;

import game.trainers.ant.daco.Colony;
import game.trainers.gradient.Newton.Uncmin_methods;
import configuration.game.trainers.DACOConfig;

public class DACOTrainer extends Trainer implements Uncmin_methods {
    private static final long serialVersionUID = 1L;

    private int maxIterations;
    private int maxStagnation;
    private boolean debugOn;
    private int populationSize;
    private double evaporationFactor;
    private double min, max;
    private double gradientWeight;

    int cnt;
    private transient Colony colony;        // colony of ants used for computation

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        DACOConfig cf = (DACOConfig) cfg;

        maxIterations = cf.getMaxIterations();
        maxStagnation = cf.getMaxStagnation();
        debugOn = cf.getDebugOn();
        populationSize = cf.getPopulationsize();
        evaporationFactor = cf.getEvaporationFactor();
        min = cf.getMin();
        max = cf.getMax();
        gradientWeight = cf.getGradientWeight();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        colony = new Colony(this, coefficients, maxIterations,
                maxStagnation, debugOn, populationSize,
                evaporationFactor, min, max, gradientWeight);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        colony.run();
        //System.out.println("# of getError() calls: " + getErrorCount);
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "DACO: Direct Ant Colony Algorithm";
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
        return DACOConfig.class;
    }

    public boolean isExecutableInParallelMode() {
        return true;
    }
}
