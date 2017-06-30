package game.trainers;

import game.trainers.palmath.MultivariateFunction;
import game.trainers.palmath.OrthogonalSearch;
import game.utils.GlobalRandom;
import configuration.game.trainers.OrthogonalSearchConfig;

public class OrthogonalSearchTrainer extends Trainer implements MultivariateFunction {
    /**
     * This is trainer for Conjugate Gradient Search
     */
    private static final long serialVersionUID = 1L;
    private transient OrthogonalSearch sos = new OrthogonalSearch();
    double lastError = -1;
    double firstError = -1;
    int cnt = 0;
    private double tolfx;
    private double tolx;
    static int counter = 0;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        OrthogonalSearchConfig cf = (OrthogonalSearchConfig) cfg;

        tolfx = cf.getTolfx();
        tolx = cf.getTolx();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        // generate new random coefficients
        for (int i = 0; i < coef; i++) {
            best[i] = GlobalRandom.getInstance().getSmallDouble();
        }
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "Orthogonal Search ";
    }

    public Class getConfigClass() {
        return OrthogonalSearchConfig.class;
    }

    public boolean allowedByDefault() {
        return false;//true;
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        //int old = counter;
        evaluate(best);
        //  System.out.println("Before : "+errorBestSoFar);
        sos.optimize(this, best, tolfx, tolx);
        // System.out.println("After : "+errorBestSoFar);
        // System.out.println(counter - old);
    }

    /**
     * returns the best configuration so far
     */
    public double[] getBest() {
        return best;
    }

    /**
     * returns the ith element of the best configuration so far
     */
    public double getBest(int index) {
        return best[index];
    }

    /**
     * returns function value
     */
    public double evaluate(double[] argument) {

        return getAndRecordError(argument, 10, 100, true);
    }

    public double getError(double[] x) {
        double err = unit.getError(x);

//		    System.out.println(err+" ");
//		    for(int i=0;i<x.length;i++) System.out.print(x[i]+" ");
//		    System.out.println();
        if (err < errorBestSoFar) {
            System.arraycopy(x, 0, best, 0, coefficients);
            errorBestSoFar = err;
        }
        return err;
    }

    public int getNumArguments() {
        return coefficients;
    }

    public double getLowerBound(int n) {
        return -Double.MIN_VALUE; //-1000;
    }

    public double getUpperBound(int n) {
        return Double.MAX_VALUE; //1000;
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
