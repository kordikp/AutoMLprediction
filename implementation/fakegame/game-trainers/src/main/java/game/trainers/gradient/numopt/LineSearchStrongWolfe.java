package game.trainers.gradient.numopt;

import common.MachineAccuracy;
import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 21.3.2007
 * Time: 19:07:34
 * <p/>
 * Line search satisfying strong Wolfe conditions:
 * (1) f(alpha) <= f(0) + c1*f'(0),
 * (2) |f(alpha)| <= -c2*f'(0).
 * <p/>
 * Where c1 < 1/2 and c1 < c2 < 1.
 * <p/>
 * Based on:
 * [1] Jorge Nocedal, Stephen J. Wright: Numerical Optimization,
 * Springer-Verlag, 1999
 * also used
 * [2] R. Fletcher: Practical Methods of Optimization, John Wiley & Sons, 1987,
 * second edition, section 2.6.
 * [3] M. Al-Baali and R. Fletcher: An Efficient Line Search for Nonlinear Least
 * Squares, Journal of Optimization Theory and Applications, 1986, Volume 48,
 * Number 3, pages 359-377.
 * <p/>
 * TODO evaluate gradient later see [1]
 */
public class LineSearchStrongWolfe extends LineSearch {
    private double c1 = 0.01;
    private double c2 = 0.9;
    //    private double c1 = 1e-4;
//    private double c2 = 0.1;
    private double tau1 = 9.0;
    private double tau3 = 0.5;

    private int evaluateCalls;

    private double tol;

    private double[] dir;

    private double[] x0;
    private double fx0;
    private double slope0;

    private double slopeAlpha;

    private double alphaOld;
    private double fAlphaOld;
    private double slopeAlphaOld;

    //alpha_lo and alpha_hi in [1]
    private double a;
    private double b;
    private double fA;
    private double fB;
    private double slopeA;
    private double slopeB;

    //maximum step size
    private double alphaMax = Double.MAX_VALUE;

    //maximum number of evaluations
    private int maxEvaluateCalls = 1000;

    private double functionTolerance = MachineAccuracy.EPSILON;

    public LineSearchStrongWolfe(ObjectiveFunction ofunc) {
        super(ofunc);
    }

    public double getFunctionTolerance() {
        return functionTolerance;
    }

    public void setFunctionTolerance(double functionTolerance) {
        this.functionTolerance = functionTolerance;
    }

    public double getAlphaMax() {
        return alphaMax;
    }

    public void setAlphaMax(double alphaMax) {
        this.alphaMax = alphaMax;
    }

    public int getMaxEvaluateCalls() {
        return maxEvaluateCalls;
    }

    public void setMaxEvaluateCalls(int maxEvaluateCalls) {
        this.maxEvaluateCalls = maxEvaluateCalls;
    }

    /**
     * @param ox0  initial point
     * @param odir line direction
     * @param ofx0 f(x0)
     * @param ogx0 gradient gAlpha(x0)
     * @return
     * @throws LineSearchException
     */
    public double minimize(double[] ox0, double[] odir, final double ofx0, double[] ogx0) throws LineSearchException {
        xAlpha = ox0;
        gAlpha = ogx0;
        x0 = xAlpha.clone();
        dir = odir;
        fx0 = ofx0;
//        System.out.print("x0 = " + x0);
//        System.out.println(" xAlpha = " + xAlpha);
        //compute slope
        slope0 = dotProduct(gAlpha, dir); //TODO rescale gAlpha?
        if (slope0 >= 0.0) {
            throw new LineSearchException("Slope not negative.");
        }

        alpha = initAlpha;
        fAlpha = fx0;
        slopeAlpha = slope0;

        alphaOld = 0.0;

        double ba; //bracketing
        double bb;

        double alphaNew; //temporary for newly interpolated alpha

        evaluateCalls = 0;

        tol = functionTolerance / 1000.0; // tolerance - experimental

        boolean firstRun = true;

        while (evaluateCalls < maxEvaluateCalls) {
            fAlphaOld = fAlpha;
            slopeAlphaOld = slopeAlpha;

            // compute new argument
            for (int i = 0; i < n; i++) {
                xAlpha[i] = x0[i] + alpha * dir[i];
            }

            // evaluate function value and gradient in f(alpha) = f(xAlpha)
            fAlpha = func.evaluate(xAlpha, gAlpha);
            evaluateCalls++; //TODO numeric derivatives
            // compute slope in alpha (xAlpha)
            slopeAlpha = dotProduct(gAlpha, dir);

            //Wolfe condition (1)
            if (fAlpha > fx0 + alpha * c1 * slope0 || (fAlpha >= fAlphaOld && !firstRun)) {
                a = alphaOld;
                b = alpha;
                fA = fAlphaOld;
                fB = fAlpha;
                slopeA = slopeAlphaOld;
                slopeB = slopeAlpha;
                zoom();
//                System.out.println("WC1");
//                checkAndPrintGoodSolution();
                return fAlpha;
            }

            firstRun = false;

            //Wolfe condition (2)
            if (Math.abs(slopeAlpha) <= -c2 * slope0) {
//                System.out.println("OK");
                //we are done
//                checkAndPrintGoodSolution();
                return fAlpha;
            }

            if (slopeAlpha >= 0.0) {
                a = alpha;
                b = alphaOld;
                fA = fAlpha;
                fB = fAlphaOld;
                slopeA = slopeAlpha;
                slopeB = slopeAlphaOld;
                zoom();
//                System.out.println("WC2: slopeAlpha >= 0.0");
//                checkAndPrintGoodSolution();
                return fAlpha;
            }

            // choose new alpha
            if ((2.0 * alpha - alphaOld) < alphaMax) {
                ba = 2.0 * alpha - alphaOld;
                bb = Math.min(alphaMax, alpha + tau1 * (alpha - alphaOld));
                alphaNew = ChooseNewAlpha(ba, bb);
                alphaOld = alpha;
                alpha = alphaNew;
            } else {
                alpha = alphaMax;
            }
        }
        //We reach this point if and only if maxFunEvals was reached
//        throw new LineSearchException("minimize: maxFunEvals was reached.");
        return (fAlpha);
    }

    /**
     * Performs bracketing.
     */
    private void zoom() {
        double tau2 = Math.min(0.1, c2);
        double aOld;
        double bOld;
        double fAOld;
        double fBOld;
        double slopeAOld;
        double slopeBOld;

        while (evaluateCalls < maxEvaluateCalls) {
            // reduce bracket
            double ba = a + tau2 * (b - a);
            double bb = b - tau3 * (b - a);
//            double ba = a;
//            double bb = b;

            alpha = ChooseNewAlpha(ba, bb);
            if (Math.abs((alpha - a) * slopeA) <= tol) {
//                System.out.println("roundoff errors: Math.abs((alpha - a) * slopeA) <= tol");
                // roundoff errors
                return;
            }

            // compute new argument
            for (int i = 0; i < n; i++) {
                xAlpha[i] = x0[i] + alpha * dir[i];
            }
            fAlpha = func.evaluate(xAlpha, gAlpha);
            evaluateCalls++; //TODO numeric derivatives
            slopeAlpha = dotProduct(gAlpha, dir); //compute slope

            // store old bracket
            aOld = a;
            bOld = b;
            fAOld = fA;
            fBOld = fB;
            slopeAOld = slopeA;
            slopeBOld = slopeB;

            //Wolfe condition (1)
            if (fAlpha > fx0 + alpha * c1 * slope0 || fAlpha >= fA) {
                a = aOld;
                b = alpha;
                fA = fAOld;
                fB = fAlpha;
                slopeA = slopeAOld;
                slopeB = slopeAlpha;
            } else {
                //Wolfe condition (2)
                if (Math.abs(slopeAlpha) <= -c2 * slope0) {
//                    System.out.println("acceptable point found");
                    //acceptable point found
                    return;
                }
                //TODO differently in [1] - can be simpler
                a = alpha;
                fA = fAlpha;
                slopeA = slopeAlpha;
                if (slopeAlpha * (b - a) >= 0.0) {
                    b = aOld;
                    fB = fAOld;
                    slopeB = slopeAOld;
                } else {
                    b = bOld;
                    fB = fBOld;
                    slopeB = slopeBOld;
                }
            }

            if (Math.abs(b - a) < MachineAccuracy.EPSILON) {
//                System.out.println("roundoff errors: Math.abs(b - a) < MachineAccuracy.EPSILON");
                //roundoff errors
                return;
            }
        }
        //We reach this point if and only if maxFunEvals was reached
    }

    public static double dotProduct(double[] ovx, double[] ovy) {
        double ddot = 0.0;

        if (ovx.length <= 0) {
            return ddot;
        }

        for (int i = 0; i < ovx.length; i++) {
            ddot += ovx[i] * ovy[i];
        }
        return ddot;
    }

    /**
     * Use cubic interpolation to find new alpha.
     *
     * @param oba
     * @param obb
     * @return
     */
    private double ChooseNewAlpha(double oba, double obb) {
        double alphaInt; //interpolated alpha
        double alphaB; // best bracket alpha
        double fAlphaB; // best bracket alpha functional value
        double ofba; //bracket functional values
        double ofbb;

        alphaInt = CubicInterpolation.interpolateAndMinimize(alphaOld, alpha, fAlphaOld, fAlpha, slopeAlphaOld, slopeAlpha);
        //TODO optimize
        ofba = CubicInterpolation.interpolate(alphaOld, alpha, fAlphaOld, fAlpha, slopeAlphaOld, slopeAlpha, oba);
        ofbb = CubicInterpolation.interpolate(alphaOld, alpha, fAlphaOld, fAlpha, slopeAlphaOld, slopeAlpha, obb);

        if (oba > obb) {
            double t = oba;
            oba = obb;
            obb = t;
            t = ofba;
            ofba = ofbb;
            ofbb = ofba;
//            System.out.println("oba>obb");
        }
        if (ofba < ofbb) {
            alphaB = oba;
            fAlphaB = ofba;
        } else {
            alphaB = obb;
            fAlphaB = ofbb;
        }

        if (oba <= alphaInt && alphaInt <= obb) {//is the minimum inside bracket?
            double fAlphaInt; //interpolated alpha function value
            fAlphaInt = CubicInterpolation.interpolate(alphaOld, alpha, fAlphaOld, fAlpha, slopeAlphaOld, slopeAlpha, alphaInt);
            if (fAlphaInt <= fAlphaB) {
                return alphaInt;
            }
        }
        return alphaB;
    }

    private void checkAndPrintGoodSolution() {
        //check for strong Wolfe
        if (!LineSearches.checkWolfeSuffcientDecrease(fx0, slope0, alpha, fAlpha, c1)) {
            System.out.println("Wolfe (1) not satisfied");
        }
        if (!LineSearches.checkStrongWolfeCurvature(slope0, alpha, c2)) {
            System.out.println("Wolfe (2) not satisfied");
        }
        if (fAlpha >= fx0) {
            System.out.println("fAlpha >= fx0");
            if (fAlpha > fx0) {
                System.out.println("fAlpha > fx0");
            }
        }

    }
}
