package game.trainers.gradient.numopt;

import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;
import common.function.ObjectiveFunctions;

/**
 * User: honza
 * Date: 17.2.2007
 * Time: 20:09:59
 */
public class LineSearchBrentNoDerivatives extends LineSearchBrent {
    // Brent's method
    private double brentGoldRatio = 0.3819660;

    public LineSearchBrentNoDerivatives(ObjectiveFunction ofunc) {
        super(ofunc);
    }

    //Brent method does not compute gradient in f(alpha) so we must get it separately
    public double[] getGAlpha() {
        gAlpha = new double[n];
        if (func.isAnalyticGradient()) {
            func.gradient(xAlpha, gAlpha);
        } else {
            NumericalDifferentiation.gradientCD(func, xAlpha, gAlpha);
        }
        return gAlpha;
    }

    public double minimize(double ox0[], double[] odir, final double ofx0, double[] ogx0) throws LineSearchException {
        double ret = minimize(ox0, odir, ofx0);
        if (func.isAnalyticGradient()) {
            func.gradient(ox0, ogx0);
        } else {
            NumericalDifferentiation.gradientCD(func, ox0, ogx0);
        }
        return ret;
    }

    public double minimize(double ox0[], double[] odir, final double ofx0) throws LineSearchException {
        xAlpha = ox0;
        ax = 0.0;
        bx = initAlpha;
        minimumBracketing(ox0, odir, ofx0);

        brentMethod(ox0, odir);

        //TODO make better (recomputation of xAlpha)
        for (int i = 0; i < n; i++) {
            xAlpha[i] = ox0[i] + alpha * odir[i];
        }
        return fAlpha;
    }

    public double minimize(double ox0[], double[] odir) throws LineSearchException {
        return minimize(ox0, odir, func.evaluate(ox0));
    }

    private void brentMethod(double[] ox0, double[] odir) throws LineSearchException {
        double a, b, etemp, fu, fv, fw, fx, p1, q1, r1, tol1, tol2, u, v, w, x, xm;
        double e = 0.0;
        double d = 0.0;

        a = (ax < cx ? ax : cx);
        b = (ax > cx ? ax : cx);
        x = w = v = bx;
        fw = fv = fx = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, x);
        for (int iter = 0; iter < brentMaxIterations; iter++) {
            xm = 0.5 * (a + b);
            tol2 = 2.0 * (tol1 = tolerance * Math.abs(x) + brentEpsilon);
            if (Math.abs(x - xm) <= (tol2 - 0.5 * (b - a))) {
                alpha = x;
                fAlpha = fx;
                return;
            }

            if (Math.abs(e) > tol1) {
                r1 = (x - w) * (fx - fv);
                q1 = (x - v) * (fx - fw);
                p1 = (x - v) * q1 - (x - w) * r1;
                q1 = 2.0 * (q1 - r1);
                if (q1 > 0.0) p1 = -p1;
                q1 = Math.abs(q1);
                etemp = e;
                e = d;
                if (Math.abs(p1) >= Math.abs(0.5 * q1 * etemp) || p1 <= q1 * (a - x) || p1 >= q1 * (b - x)) {
                    d = brentGoldRatio * (e = (x >= xm ? a - x : b - x));
                } else {
                    d = p1 / q1;
                    u = x + d;
                    if (u - a < tol2 || b - u < tol2) {
                        if (xm >= x) {
                            d = Math.abs(tol1);
                        } else {
                            d = -Math.abs(tol1);
                        }
                    }
                }
            } else {
                d = brentGoldRatio * (e = (x >= xm ? a - x : b - x));
            }
            if (d >= 0.0) {
                u = (Math.abs(d) >= tol1 ? x + d : x + Math.abs(tol1));
            } else {
                u = (Math.abs(d) >= tol1 ? x + d : x - Math.abs(tol1));
            }

            fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
            if (fu <= fx) {
                if (u >= x) {
                    a = x;
                } else {
                    b = x;
                }
                v = w;
                w = x;
                x = u;

                fv = fw;
                fw = fx;
                fx = fu;
            } else {
                if (u < x) {
                    a = u;
                } else {
                    b = u;
                }
                if (fu <= fw || w == x) {
                    v = w;
                    w = u;
                    fv = fw;
                    fw = fu;
                } else if (fu <= fv || v == x || v == w) {
                    v = u;
                    fv = fu;
                }
            }
        }
        throw new LineSearchException("brentMethod: Too many iterations.");
    }
}
