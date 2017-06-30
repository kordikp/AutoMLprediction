package game.trainers;

import game.trainers.gradient.Powell.PowellMinimiser;

/**
 * Created by IntelliJ IDEA.
 * User: kordikp
 */
class PowellMinimiserImpl extends PowellMinimiser {
    private PowellTrainer source;
    private int d;
    private double[] cparams;

    public void setIterMax(int i) {
        ITMAX = i;
    }

    public int getDim() {
        return d;
    }

    PowellMinimiserImpl(PowellTrainer src, int dimension) {
        super(dimension);
        cparams = new double[dimension];
        d = dimension;
        source = src;
    }

    protected double fObj(double[] params) {
        for (int i = 1; i <= d; i++) cparams[i - 1] = params[i];
        return source.getError(cparams);
    }
}
