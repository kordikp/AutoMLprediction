package game.trainers;

import game.trainers.palmath.DifferentialEvolution;
import game.trainers.palmath.MultivariateFunction;
import game.utils.GlobalRandom;
import configuration.game.trainers.PALDifferentialEvolutionConfig;

public class PALDifferentialEvolutionTrainer extends Trainer implements MultivariateFunction {
    /**
     * This is trainer for Conjugate Gradient Search
     */
    private static final long serialVersionUID = 1L;
    private transient GradientTrainable unit;
    private int coefficients;
    private double[] best;
    private double errorBestSoFar;
    private transient DifferentialEvolution de;

    double lastError = -1;
    double firstError = -1;
    int cnt = 0;
    private double tolfx = 0.1;
    private double tolx = 0.1;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        //todo  PALDifferentialEvolutionConfig dec = (PALDifferentialEvolutionConfig)cfg;
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        de = new DifferentialEvolution(coef);
        // generate new random coefficients
        for (int i = 0; i < coef; i++) {
            best[i] = GlobalRandom.getInstance().getSmallDouble();
        }
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "PAL: Differential Evolution";
    }

    public Class getConfigClass() {
        return PALDifferentialEvolutionConfig.class;
    }

    public boolean allowedByDefault() {
        return false;
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        //  System.out.println("Before optimization : "+errorBestSoFar);
        de.optimize(this, best, tolfx, tolx);
        //  System.out.println("After optimization : "+errorBestSoFar);
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

    // TODO
    public double evaluate(double[] argument) {
        return getAndRecordError(argument, 10, 100, true);
    }

    public double getError(double[] x) {
        double err = unit.getError(x);
        if (err < errorBestSoFar) {
            System.arraycopy(x, 0, best, 0, coefficients);
        }
        return err;
    }

    public int getNumArguments() {
        return coefficients;
    }

    public double getLowerBound(int n) {
        return -10000;
    }

    public double getUpperBound(int n) {
        return 10000;
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
