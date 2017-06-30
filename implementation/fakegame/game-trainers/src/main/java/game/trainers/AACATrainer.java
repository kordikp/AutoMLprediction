/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: Adaptive Ant Colony Algorithm (AACA) - Trainer</p>
 * <p>Description: trainer class for AACA</p>
 */

package game.trainers;

import game.trainers.ant.aaca.Colony;
import game.trainers.gradient.Newton.Uncmin_methods;
import configuration.game.trainers.AACAConfig;

public class AACATrainer extends Trainer implements Uncmin_methods {

    private static final long serialVersionUID = 1L;

    private int maxIterations;
    private int maxStagnation;
    private boolean debugOn;
    private int populationSize;
    private int encodingLength;
    private double evaporationFactor;
    private double pheromoneIndex;
    private double costIndex;
    private double min, max;
    private double gradientWeight;

    private transient Colony colony;        // colony of ants used for computation

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        AACAConfig cf = (AACAConfig) cfg;
        maxIterations = cf.getMaxIterations();
        maxStagnation = cf.getMaxStagnation();
        debugOn = cf.getDebugOn();
        populationSize = cf.getPopulationsize();
        encodingLength = cf.getEncodingLength();
        evaporationFactor = cf.getEvaporationFactor();
        pheromoneIndex = cf.getPheromoneIndex();
        costIndex = cf.getCostIndex();
        min = cf.getMin();
        max = cf.getMax();
        gradientWeight = cf.getGradientWeight();

    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        colony = new Colony(this, coefficients, maxIterations,
                maxStagnation, debugOn, populationSize,
                encodingLength, evaporationFactor, pheromoneIndex,
                costIndex, min, max, gradientWeight);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        colony.run();
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "AACA: Adaptive Ant Colony Algorithm";
    }

    /**
     * returns error of vector x
     */
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
        return AACAConfig.class;
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
