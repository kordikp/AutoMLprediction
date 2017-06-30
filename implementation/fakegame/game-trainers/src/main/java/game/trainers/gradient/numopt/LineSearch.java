package game.trainers.gradient.numopt;

import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 3.5.2007
 * Time: 18:11:36
 */
public abstract class LineSearch {
    double alpha = 0.0;
    double[] xAlpha;
    double fAlpha;
    double[] gAlpha;

    //initial step size
    double initAlpha = 1.0;

    ObjectiveFunction func;
    int n;

    LineSearch(ObjectiveFunction ofunc) {
        func = ofunc;
        n = ofunc.getNumArguments();
    }

    public double getAlpha() {
        return alpha;
    }

    public double getInitAlpha() {
        return initAlpha;
    }

    public void setInitAlpha(double initAlpha) {
        this.initAlpha = initAlpha;
    }

    public abstract double minimize(double ox0[], double[] odir, final double ofx0, double[] ogx0) throws LineSearchException;

    public double minimize(double ox0[], double[] odir, final double ofx0) throws LineSearchException {
        double[] gx0 = new double[n];
        if (func.isAnalyticGradient()) {
            func.gradient(ox0, gx0);
        } else {
            NumericalDifferentiation.gradientCD(func, ox0, gx0);
        }
        return minimize(ox0, odir, ofx0, gx0);
    }

    public double minimize(double ox0[], double[] odir) throws LineSearchException {
        double fx0;
        double[] gx0 = new double[n];
        if (func.isAnalyticGradient()) {
            fx0 = func.evaluate(ox0, gx0);
        } else {
            fx0 = func.evaluate(ox0);
            NumericalDifferentiation.gradientCD(func, ox0, gx0);
        }
        return minimize(ox0, odir, fx0, gx0);
    }
}
