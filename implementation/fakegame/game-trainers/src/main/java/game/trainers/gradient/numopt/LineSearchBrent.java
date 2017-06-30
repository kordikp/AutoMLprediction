package game.trainers.gradient.numopt;

import common.MachineAccuracy;
import common.function.ObjectiveFunction;
import common.function.ObjectiveFunctions;

/**
 * User: honza
 * Date: 17.2.2007
 * Time: 20:09:59
 */
public abstract class LineSearchBrent extends LineSearch {
    //    private double tolerance = 2.0e-4;
    double tolerance = MachineAccuracy.SQRT_EPSILON;

    // bracketing
    private double goldRatio = 1.618034;
    private double goldLimit = 1e2;
    private double tiny = 1.0e-20;

    double ax;
    double bx;
    double cx;

    // Brent's method
    double brentMaxIterations = 100;
    private double brentGoldRatio = 0.3819660;
    //    private double brentEpsilon = 1.0e-10;
    double brentEpsilon = 1.0E-15;

    LineSearchBrent(ObjectiveFunction ofunc) {
        super(ofunc);
    }

    void minimumBracketing(double ox0[], double[] odir, double ofx0) throws LineSearchException {
        double ulim, u, r, q, fu;

        double fb = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, bx);

        if (fb > ofx0) {
            double dum = ax;
            ax = bx;
            bx = dum;
            dum = fb;
            fb = ofx0;
            ofx0 = dum;
        }

        cx = bx + goldRatio * (bx - ax);
        double fc = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, cx);

        double qmr;
        while (fb > fc) {
            r = (bx - ax) * (fb - fc);
            q = (bx - cx) * (fb - ofx0);

            qmr = q - r;
            if (qmr >= 0.0) {
                u = bx - ((bx - cx) * q - (bx - ax) * r) / (2.0 * Math.max(Math.abs(qmr), tiny));
            } else {
                u = bx - ((bx - cx) * q - (bx - ax) * r) / (-2.0 * Math.max(Math.abs(qmr), tiny));
            }

            ulim = bx + goldLimit * (cx - bx);
            if ((bx - u) * (u - cx) > 0.0) {
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
                if (fu < fc) {
                    ax = bx;
                    bx = u;
//                    ofx0 = fb;
//                    fb = fu;
                    return;
                } else if (fu > fb) {
                    cx = u;
//                    fc = fu;
                    return;
                }
                u = cx + goldRatio * (cx - bx);
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
            } else if ((cx - u) * (u - ulim) > 0.0) {
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
                if (fu < fc) {
                    bx = cx;
                    cx = u;
                    u = cx + goldRatio * (cx - bx);

                    fb = fc;
                    fc = fu;
                    fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
                }
            } else if ((u - ulim) * (ulim - cx) >= 0.0) {
                u = ulim;
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
            } else {
                u = cx + goldRatio * (cx - bx);
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
            }
            ax = bx;
            bx = cx;
            cx = u;

            ofx0 = fb;
            fb = fc;
            fc = fu;
        }
    }
}
