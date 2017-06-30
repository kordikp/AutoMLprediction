package common.function;

import common.MachineAccuracy;

/**
 * User: honza
 * Date: 19.2.2007
 * Time: 16:31:04
 */
public class NumericalDifferentiation {

    private static double gradientCDStepMult = Math.pow(MachineAccuracy.EPSILON, 1.0 / 3.0);

    /**
     * Determine gradient numerically using central difference. (PAL, NR, uncmin inspired)
     * Needs 2*n function evaluations.
     *
     * @param ofunc objective function
     * @param oarg  argument vector
     * @param ograd vector for gradientCD
     */
    public static void gradientCD(ObjectiveFunction ofunc, double[] oarg, double[] ograd) {
        double oldx, h, fxplus, fxminus;
        for (int i = 0; i < ofunc.getNumArguments(); i++) {
            oldx = oarg[i];
            h = gradientCDStepMult * Math.abs(oldx);

            oarg[i] = oldx + h;
            fxplus = ofunc.evaluate(oarg);
            oarg[i] = oldx - h;
            fxminus = ofunc.evaluate(oarg);
            oarg[i] = oldx;

            // first derivative
            ograd[i] = (fxplus - fxminus) / (2.0 * h);
        }
    }

    /**
     * Determine gradient numerically using forward finite difference. (NR, uncmin inspired)
     * Needs n function evaluations.
     *
     * @param ofunc objective function
     * @param oarg  argument vector
     * @param ofx   evaluated function value ofunc(oarg)
     * @param ograd vector for gradientCD
     */
    private static void gradientFD(ObjectiveFunction ofunc, double[] oarg, double ofx, double[] ograd) {
        double oldx, h, fxplus;
        for (int i = 0; i < ofunc.getNumArguments(); i++) {
            oldx = oarg[i];
            h = MachineAccuracy.SQRT_EPSILON * Math.abs(oldx);

            oarg[i] = oldx + h;
            fxplus = ofunc.evaluate(oarg);
            oarg[i] = oldx;

            // first derivative
            ograd[i] = (fxplus - ofx) / h;
        }
    }

    /**
     * Determine gradient numerically using forward finite difference. (NR, uncmin inspired)
     * Needs n function evaluations. This method computes function value in oarg.
     *
     * @param ofunc objective function
     * @param oarg  argument vector
     * @param ograd vector for gradientCD
     */
    private static void gradientFD(ObjectiveFunction ofunc, double[] oarg, double[] ograd) {
        gradientFD(ofunc, oarg, ofunc.evaluate(oarg), ograd);
    }

    public static void hessian(ObjectiveFunction ofunc, double[] oarg, double[][] ohessian) {
        System.out.println("NumericalDifferentiation.hessian: not yet implemented!");
    }

    public static boolean checkAnalyticGradient(ObjectiveFunction ofunc, double[] oarg) {
        if (!ofunc.isAnalyticGradient()) {
            throw new IllegalArgumentException("Function has no analytic gradient");
        }
        double[] ngrad = new double[oarg.length];
        double[] ngrad2 = new double[oarg.length];
        double[] agrad = new double[oarg.length];

        gradientFD(ofunc, oarg, ngrad);
        gradientCD(ofunc, oarg, ngrad2);
        ofunc.gradient(oarg, agrad);

        for (int i = 0; i < agrad.length; i++) {
            System.out.println(i + ": " + " " + agrad[i] + " "
                    + ngrad[i] + " " + Math.abs(ngrad[i] - agrad[i]) + " "
                    + ngrad2[i] + " " + Math.abs(ngrad2[i] - agrad[i]));
        }

        boolean isOk = true;
        for (int i = 0; i < agrad.length; i++) {
            if (Math.abs(agrad[i] - ngrad[i]) > 1) {
                isOk = false;
            }

        }
        return isOk;
    }
}
