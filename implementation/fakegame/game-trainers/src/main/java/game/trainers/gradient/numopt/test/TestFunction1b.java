package game.trainers.gradient.numopt.test;

import common.function.BasicObjectiveFunction;

/**
 * User: honza
 * Date: 8.3.2007
 * Time: 23:57:09
 * To change this template use File | Settings | File Templates.
 */
public class TestFunction1b extends BasicObjectiveFunction {
    public int getNumArguments() {
        return 1;
    }

    public double evaluate(double[] oargument) {
        double x = oargument[0];
        return -Math.cos(x);
    }

    public void gradient(double[] oargument, double[] ogradient) {
        double x = oargument[0];
        ogradient[0] = Math.sin(x);
    }

    public void hessian(double[] oargument, double[][] ohessian) {
        double x = oargument[0];
        ohessian[0][0] = Math.cos(x);
    }

    public boolean isAnalyticGradient() {
        return true;
    }

    public boolean isAnalyticHessian() {
        return true;
    }
}
