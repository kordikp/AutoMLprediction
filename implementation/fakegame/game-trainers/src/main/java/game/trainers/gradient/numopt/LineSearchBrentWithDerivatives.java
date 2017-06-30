package game.trainers.gradient.numopt;

import common.function.ObjectiveFunction;
import common.function.ObjectiveFunctions;

/**
 * User: drchaj1
 * Date: 3.5.2007
 * Time: 17:51:44
 */
public class LineSearchBrentWithDerivatives extends LineSearchBrent {
    //    private double tolerance = 2.0e-4;

    public LineSearchBrentWithDerivatives(ObjectiveFunction ofunc) {
        super(ofunc);
        if (!func.isAnalyticGradient()) {
            System.out.println("Warning: LineSearchBrentWithDerivatives: function has not analytic gradient");
        }
    }

    public double minimize(double ox0[], double[] odir, final double ofx0, double[] ogx0) throws LineSearchException {
        xAlpha = ox0;
        gAlpha = ogx0;
        ax = 0.0;
        bx = initAlpha;
        minimumBracketing(ox0, odir, ofx0);

        brentMethodWithDerivatives(ox0, odir);

        //TODO make better (recomputation of xAlpha)
        for (int i = 0; i < n; i++) {
            xAlpha[i] = ox0[i] + alpha * odir[i];
        }
        return fAlpha;
    }

    void brentMethodWithDerivatives(double[] ox0, double[] odir) throws LineSearchException {
        int iter;
        boolean ok1, ok2;
        double a, b, d = 0.0, d1, d2, du, dv, dw, dx, e = 0.0;
        double fu, fv, fw, fx, olde, tol1, tol2, u, u1, u2, v, w, x, xm;

        a = (ax < cx ? ax : cx);
        b = (ax > cx ? ax : cx);
        x = w = v = bx;
        fw = fv = fx = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, x);
        dw = dv = dx = ObjectiveFunctions.evaluateDerivativeAlongDirection(func, ox0, odir, x, gAlpha);
        for (iter = 0; iter < brentMaxIterations; iter++) {
            xm = 0.5 * (a + b);
            tol1 = tolerance * Math.abs(x) + brentEpsilon;
            tol2 = 2.0 * tol1;
            if (Math.abs(x - xm) <= (tol2 - 0.5 * (b - a))) {
                alpha = x;
                fAlpha = fx;
                return;
            }
            if (Math.abs(e) > tol1) {
                d1 = 2.0 * (b - a);
                d2 = d1;
                if (dw != dx) d1 = (w - x) * dx / (dx - dw);
                if (dv != dx) d2 = (v - x) * dx / (dx - dv);
                u1 = x + d1;
                u2 = x + d2;
                ok1 = (a - u1) * (u1 - b) > 0.0 && dx * d1 <= 0.0;
                ok2 = (a - u2) * (u2 - b) > 0.0 && dx * d2 <= 0.0;
                olde = e;
                e = d;
                if (ok1 || ok2) {
                    if (ok1 && ok2) {
                        d = (Math.abs(d1) < Math.abs(d2) ? d1 : d2);
                    } else if (ok1) {
                        d = d1;
                    } else {
                        d = d2;
                    }
                    if (Math.abs(d) <= Math.abs(0.5 * olde)) {
                        u = x + d;
                        if (u - a < tol2 || b - u < tol2) {
                            d = xm >= x ? Math.abs(tol1) : -Math.abs(tol1);
                        }
                    } else {
                        d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
                    }
                } else {
                    d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
                }
            } else {
                d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
            }
            if (Math.abs(d) >= tol1) {
                u = x + d;
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
            } else {
                u = d >= 0 ? x + Math.abs(tol1) : x - Math.abs(tol1);
                fu = ObjectiveFunctions.evaluateFunctionAlongDirection(func, ox0, odir, u);
                if (fu > fx) {
                    alpha = x;
                    fAlpha = fx;
                    return;
                }
            }
            du = ObjectiveFunctions.evaluateDerivativeAlongDirection(func, ox0, odir, u, gAlpha);
            if (fu <= fx) {
                if (u >= x) {
                    a = x;
                } else {
                    b = x;
                }
                v = w;
                fv = fw;
                dv = dw;

                w = x;
                fw = fx;
                dw = dx;

                x = u;
                fx = fu;
                dx = du;
            } else {
                if (u < x) {
                    a = u;
                } else {
                    b = u;
                }
                if (fu <= fw || w == x) {
                    v = w;
                    fv = fw;
                    dv = dw;

                    w = u;
                    fw = fu;
                    dw = du;
                } else if (fu < fv || v == x || v == w) {
                    v = u;
                    fv = fu;
                    dv = du;
                }
            }
        }
        throw new LineSearchException("brentMethodWithDerivatives: Too many iterations.");
    }

}
