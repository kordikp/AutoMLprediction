package game.trainers.gradient.numopt.test;

import common.function.BasicObjectiveFunction;

/**
 * User: honza
 * Date: 8.3.2007
 * Time: 23:22:55
 * f(x) = x^2
 */
public class TestFunction1 extends BasicObjectiveFunction {
    public int getNumArguments() {
        return 1;
    }

    public double evaluate(double[] oargument) {
        double x = oargument[0];
        return x * x;
    }

    public void gradient(double[] oargument, double[] ogradient) {
        double x = oargument[0];
        ogradient[0] = 2 * x;
    }

    public void hessian(double[] oargument, double[][] ohessian) {
        ohessian[0][0] = 2;
    }

    public boolean isAnalyticGradient() {
        return true;
    }

    public boolean isAnalyticHessian() {
        return true;
    }
}
