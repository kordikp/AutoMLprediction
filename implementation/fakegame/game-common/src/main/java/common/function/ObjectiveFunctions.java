package common.function;

/**
 * User: honza
 * Date: 17.2.2007
 * Time: 20:22:03
 */
public class ObjectiveFunctions {
    //TODO create method which evaluates function and its derivation together

    public static double evaluateFunctionAlongDirection(ObjectiveFunction ofunc, double[] oarg, double[] odir, double oalpha) {
        int n = ofunc.getNumArguments();
        double[] argNew = new double[n];
        for (int j = 0; j < n; j++) {
            argNew[j] = oarg[j] + oalpha * odir[j];
        }
        return ofunc.evaluate(argNew);
    }

    public static double evaluateDerivativeAlongDirection(ObjectiveFunction ofunc, double[] oarg, double[] odir, double oalpha, double[] ogAlpha) {
        int n = ofunc.getNumArguments();
        int i;
        double df1 = 0.0;
        double[] xt = new double[n];

        for (i = 0; i < n; i++) {
            xt[i] = oarg[i] + oalpha * odir[i];
        }
        if (ofunc.isAnalyticGradient()) {
            ofunc.gradient(xt, ogAlpha);
        } else {
            NumericalDifferentiation.gradientCD(ofunc, xt, ogAlpha);
        }

        for (i = 0; i < n; i++) {
            df1 += ogAlpha[i] * odir[i];
        }
        return df1;
    }
}
