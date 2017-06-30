package game.trainers;

import game.trainers.gradient.NumericalDiffWrapper;
import game.trainers.gradient.Marquardt.MarquardtMinimiser;


class LevenbergMarquardtMinimiserImpl extends MarquardtMinimiser {
    private LevenbergMarquardtTrainer sourceTrainer;
    private NumericalDiffWrapper wrap;
    private int d;
    private double[] cparams;
    private double[] cgrad;
    private double[][] chessian;

    public int getDim() {
        return d;
    }

    LevenbergMarquardtMinimiserImpl(LevenbergMarquardtTrainer sourceTrainer, NumericalDiffWrapper wrap, int dimension) {
        init(dimension);
        cparams = new double[dimension];
        cgrad = new double[dimension];
        chessian = new double[dimension][dimension];
        d = dimension;
        this.sourceTrainer = sourceTrainer;
        this.wrap = wrap;
    }

    protected double fObj(double[] atry, double[] dfda, double[][] d2fda2) {
        for (int i = 1; i <= d; i++) {
            cparams[i - 1] = atry[i];
        }
        // when analytic not supplied, compute numerical gradient
        if (!sourceTrainer.unit.gradient(cparams, cgrad)) {
//            System.out.println("GRAD");
            wrap.grad(cparams, cgrad);
        }
        System.arraycopy(cgrad, 0, dfda, 1, d);

        // when analytic not supplied, compute numerical hessian
        if (!sourceTrainer.unit.hessian(cparams, chessian)) {
//            System.out.println("HESS");
            wrap.hess(cparams, chessian);
        }

        for (int i = 1; i <= d; i++) {
            System.arraycopy(chessian[i - 1], 0, d2fda2[i], 1, d);
        }

        return sourceTrainer.getError(cparams);
    }
}