/**
 * @author Ondeej F?l?pek
 * @version 0.1
 */

package game.trainers;

import game.trainers.palmath.ConjugateGradientSearch;
import game.utils.GlobalRandom;
import configuration.game.trainers.PALConjugateGradientConfig;

/**
 * Trainer with Conjugate Gradient as optimization algorithm
 */
public class PALConjugateGradientTrainer extends Trainer implements game.trainers.palmath.MFWithGradient {
//public class PALConjugateGradientTrainer extends Trainer implements pal.math.MultivariateFunction {

    /**
     * This is trainer for Conjugate Gradient
     */
    private static final long serialVersionUID = 1L;

    private transient GradientTrainable unit;
    private transient ConjugateGradientSearch cgs;
    private transient double tolfx;
    private transient double tolx;

    private double lastError = -1;
    double firstError = -1;

    private static long teachCalls = 0;
    private static long errorCalls = 0;
    private static long gradCalls = 0;
    private static boolean verbose = false;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        PALConjugateGradientConfig cf = (PALConjugateGradientConfig) cfg;
        tolfx = cf.getTolfx();
        tolx = cf.getTolx();
        cgs = new ConjugateGradientSearch(cf.getMethod(), cf.getMaxIterations());
    }

    public void setCoef(int coef) {
        super.setCoef(coef);

        for (int i = 0; i < coef; i++) {
            best[i] = GlobalRandom.getInstance().getSmallDouble();
        }
    }

    /**
     * Return name algorithm used for weights(coeffs.) estimation
     *
     * @return name of algorithm
     */
    public String getMethodName() {
        return "PAL : Conjugate Gradient";
    }


    /**
     * Get name of config class
     *
     * @return name of config class
     */
    public Class getConfigClass() {
        return PALConjugateGradientConfig.class;
    }

    /**
     * Allow this optimization by default configuration
     *
     * @return TRUE if the method is allowed by default
     */
    public boolean allowedByDefault() {
        return false;
    }

    /**
     * Starts the teaching process
     */
    public void teach() {
        teachCalls++;
        cgs.optimize(this, best, tolfx, tolx);
        if (verbose && teachCalls >= 100) {
            System.out.println("CG - error : " + errorCalls + " and gradient : " + gradCalls + " times");
            teachCalls = 0;
            errorCalls = 0;
            gradCalls = 0;
        }
    }

    /**
     * Get best configuration so far
     *
     * @return best configuration
     */
    public double[] getBest() {
        return best;
    }

    /**
     * Returns function value of the evaluated method with actual configuration
     *
     * @param xvec actual configuration of the unit
     * @return error of the unit
     */
    public double evaluate(double[] xvec) {
        errorCalls++;
        return getAndRecordError(xvec, 10, 100, true);
    }

    /**
     * Returns function value of the evaluated method with actual parameters
     *
     * @param gvec gradient of the error function
     * @param xvec actual configuration of the unit
     * @return error of the unit
     */
    public double evaluate(double[] xvec, double[] gvec) {
        computeGradient(xvec, gvec);
        return evaluate(xvec);
    }


    /**
     * Returns actual error
     *
     * @param xvec actual configuration of the unit
     * @return actual error
     */
    public double getError(double[] xvec) {
        double err = unit.getError(xvec);
        if (err < lastError) {
            System.arraycopy(xvec, 0, best, 0, coefficients);
        }
        return err;
    }

    /**
     * Get lower bound of argument n
     *
     * @param n argument number
     * @return lower bound
     */
    public double getLowerBound(int n) {
        return -1000;
    }

    /**
     * Get upper bound of argument n
     *
     * @param n argument number
     * @return upper bound
     */
    public double getUpperBound(int n) {
        return 1000;
    }

    /**
     * Returns number of arguments of the error function
     *
     * @return number of arguments
     */
    public int getNumArguments() {
        return coefficients;
    }

    /**
     * Compute gradient of the error function
     *
     * @param xvec actual configuration of the unit
     * @param gvec gradient of the error function
     */
    public void computeGradient(double[] xvec, double[] gvec) {
        gradCalls++;
        gvec[0] = Double.MAX_VALUE;
        // gradient primo od neuronu
        unit.gradient(xvec, gvec);
        if (gvec[0] == Double.MAX_VALUE) game.trainers.palmath.NumericalDerivative.gradient(this, xvec, gvec);
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
