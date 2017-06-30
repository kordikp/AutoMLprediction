package game.trainers;

import game.trainers.palmath.MultivariateFunction;
import game.trainers.palmath.StochasticOSearch;
import game.utils.GlobalRandom;
import configuration.game.trainers.StochasticOSearchConfig;


public class StochasticOSearchTrainer extends Trainer implements MultivariateFunction {
    /**
     * This is trainer for Conjugate Gradient Search
     */
    private static final long serialVersionUID = 1L;
    private transient StochasticOSearch sos = new StochasticOSearch();
    // private StochasticOSearchConfig sosc; //= new ConjugateGradientSearchConfig();
    double lastError = -1;
    double firstError = -1;
    int cnt = 0;
    private double tolfx;
    private double tolx;
    static int counter = 0;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        StochasticOSearchConfig sosc = (StochasticOSearchConfig) cfg;
        tolfx = sosc.getTolfx();
        tolx = sosc.getTolx();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);

        for (int i = 0; i < coef; i++) {
            best[i] = GlobalRandom.getInstance().getSmallDouble();
        }
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "Stochastic Orthogonal Search ";
    }

    public Class getConfigClass() {
        return StochasticOSearchConfig.class;
    }

    public boolean allowedByDefault() {
        return false; //true;
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        //int old = counter;
        //  System.out.println("Before : "+errorBestSoFar);
        sos.optimize(this, best, tolfx, tolx);
        //  System.out.println("After : "+errorBestSoFar);
        //  System.out.println(counter - old);
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
        //counter++;
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
        return -1000;
    }

    public double getUpperBound(int n) {
        return 1000;
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
