package game.trainers.gradient;

import game.trainers.Trainer;
import cz.cvut.felk.cig.jcool.core.ObjectiveFunctionFast;
import cz.cvut.felk.cig.jcool.utils.NumericalDifferentiation;


/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Jul 28, 2009
 * Time: 12:54:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumericalDiffWrapper extends ObjectiveFunctionFast {
    final private Trainer trainer;
    final private double gradStepMult;
    final private double hessStepMult;

    public NumericalDiffWrapper(Trainer trainer, double gradStepMult, double hessStepMult) {
        this.trainer = trainer;
        this.gradStepMult = gradStepMult;
        this.hessStepMult = hessStepMult;
    }

    @Override
    public double f(double[] x) {
        double error = trainer.getAndRecordError(x, 10, 100, true);
//        System.out.println("error = " + error + " " + Arrays.toString(x));
        return error;
    }

    @Override
    public void grad(double[] x, double[] grad) {
        NumericalDifferentiation.gradientCD(this, x, grad);
    }

    @Override
    public void hess(double[] x, double[][] hessian) {
        double fx = f(x);
        NumericalDifferentiation.hessianCD(this, x, fx, hessian);
    }

    @Override
    public int getDim() {
        // return ((Neuron)trainer.getUnit()).getCoefsNumber();
        return 0;
    }

    @Override
    public boolean hasAnalyticGradient() {
        return false;
    }

    @Override
    public boolean hasAnalyticHessian() {
        return false;
    }
}
