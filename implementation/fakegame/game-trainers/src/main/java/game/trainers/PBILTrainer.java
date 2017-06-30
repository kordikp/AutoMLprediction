package game.trainers;

import game.trainers.gradient.Newton.Uncmin_methods;
import game.trainers.pbil.PBIL;
import configuration.game.trainers.PBILConfig;

/**
 * @author oleg.kovarik@gmail.com
 * @version 0.1
 *          <p>
 *          <p>Title: Population-based incremental learning (PBIL) - Trainer</p>
 *          <p>Description: trainer class for PBIL</p>
 */

public class PBILTrainer extends Trainer implements Uncmin_methods {
    private static final long serialVersionUID = 1L;
    // private double firstError, lastError;
    int cnt;
    private transient PBIL algoritm;        // PBIL algorithm

    private int maxIterations;
    private int maxStagnation;
    private boolean debugOn;
    private double min, max;
    private int bitsPerVariable;
    private int populationSize;
    private double learnRate;
    private double negLearnRate;
    private double mutProb;
    private double mutShift;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);

        PBILConfig cf = (PBILConfig) cfg;
        maxIterations = cf.getMaxIterations();
        maxStagnation = cf.getMaxStagnation();
        debugOn = cf.getDebugOn();
        min = cf.getMin();
        max = cf.getMax();
        bitsPerVariable = cf.getBitsPerVariable();
        populationSize = cf.getPopulationSize();
        learnRate = cf.getLearnRate();
        negLearnRate = cf.getNegLearnRate();
        mutProb = cf.getMutProb();
        mutShift = cf.getMutShift();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        algoritm = new PBIL(this, coefficients, maxIterations,
                maxStagnation, min, max, bitsPerVariable,
                populationSize, learnRate, negLearnRate,
                mutProb, mutShift, debugOn);
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        algoritm.run();
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "PBIL";
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
        return PBILConfig.class;
    }

    public boolean isExecutableInParallelMode() {
        return true;
    }
}
