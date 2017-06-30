package game.models.single;

import game.utils.Utils;
import configuration.models.single.GaussianNormModelConfig;


/**
 * GaussianNormModel allows to build model with gaussian transfer function.
 * It also provides the grandient of the error function
 */
public class GaussianNormModel extends SingleModel {
    GaussianNormModelConfig cfg;

    public void setInputsNumber(int inputs) {
        coef = inputs +1;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
         double d1, d2;
        double outValue = 0;
        for (int i = 0; i < inputsNumber; i++) {
                d1 = input_vector[i];
                d1 -= a[i];
                outValue += d1 * d1;
            }
        d1 = -outValue;
        d2 = (1 + a[inputsNumber]);
        d2 *= d2;
        outValue = Math.exp(d1 / d2);
        return outValue;
    }

    public double getError(double x[]) {
        double error = 0, dev, d1, d2,d3;
        for (int i = 0; i < learning_vectors; i++) {
            dev = 0;
            for (int j = 0; j < inputsNumber; j++) {
                d1 = inputVect[i][j];
                d1 -= x[j];
                dev += d1 * d1;
            }
            d1 = -dev;
            d2 = (1 + x[inputsNumber]);
            d2 *= d2;
            d3 = java.lang.Math.exp(d1 / d2);
            dev = d3;
            dev -= target[i];
            error += dev*dev;
        }
        return error;
    }
    
    public boolean gradient(double x[], double g[]) {
         double dev, d1, d2, d3;
        for (int j = 0; j < coef; j++) {
            g[j] = 0;
        }
        for (int i = 0; i < learning_vectors; i++) {
            dev = 0;
            for (int j = 0; j < inputsNumber; j++) {
                d1 = inputVect[i][j];
                d1 -= x[j];
                dev += d1 * d1;
            }
            d1 = -dev;
            d2 = (1 + x[inputsNumber]);
            d2 *= d2;
            d3 = java.lang.Math.exp(d1 / d2);
            dev = d3;
            dev -= target[i];
            dev *= 2;
            g[inputsNumber] += dev * d3 * -2 * d1 / (d2 * (1 + x[inputsNumber]));
            for (int j = 0; j < inputsNumber; j++) {
                g[j] += dev * d3 * 2 * (inputVect[i][j] - x[j]) / d2;
            }
        }
         return true;
    }
     public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += "power(" + inputEquation[i] + ((a[i]<0)?"+" + Utils.convertDouble(-a[i]): Utils.convertDouble(-a[i])) + ";2)"+((i== inputsNumber -1)?"":"+");
        }
        s = "exp(-(" + s + ")/power(" + Utils.convertDouble(1 + a[inputsNumber]) + ";2)";
        return s;
    }


    @Override
    public Class getConfigClass() {
        return GaussianNormModelConfig.class;
    }
}