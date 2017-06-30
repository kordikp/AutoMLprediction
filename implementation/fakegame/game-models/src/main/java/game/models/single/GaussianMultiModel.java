package game.models.single;

import game.utils.Utils;
import configuration.models.ModelConfig;
import configuration.models.single.GaussianMultiModelConfig;


/**
 * GaussianNormModel allows to build model with gaussian transfer function.
 * It also provides the grandient of the error function
 */
public class GaussianMultiModel extends SingleModel {

    public void init(ModelConfig cfgi) {
        super.init(cfgi);
        GaussianMultiModelConfig cfg = (GaussianMultiModelConfig) cfgi;
        
    }

    public void setInputsNumber(int inputs) {
        coef = 2 * inputs;
        a = new double[coef];
        gradient = new double[coef];

        super.setInputsNumber(inputs);
    }

     @Override
     public double[] computeStartingPoint() {
        double[] sp = new double[coef];
        double tmin = Double.MAX_VALUE;
        double tmax = Double.MIN_VALUE;
        int idx_max = 0;
        for (int i = 0; i < learning_vectors; i++) {
            if (target[i] < tmin) tmin = target[i];
            if (target[i] > tmax) {
                tmax = target[i];
                idx_max = i;
            }
        }
        System.arraycopy(inputVect[idx_max], 0, sp, 0, inputsNumber);

        for (int i = 0; i < inputsNumber; i++) {
            sp[i + inputsNumber] = 1;
        }
        return sp;
    }

    @Override
    protected double getOutputWith(double[] input_vector, double[] a) {
        double d1;
        double outValue = 0;
        for (int i = 0; i < inputsNumber; i++) {
            d1 = input_vector[i];
            d1 -= a[i];
            outValue += d1 * d1 / (2 * a[i + inputsNumber] * a[i + inputsNumber]);
        }
        outValue = Math.exp(-outValue);
        return outValue;
    }

    public boolean gradient(double x[], double g[]) {
              double dev, y,d1;
       for (int j = 0; j < coef; j++) {
           g[j] = 0;
       }
       for (int i = 0; i < learning_vectors; i++) {
           if (inValidationSet[i]) continue;
           dev = 0;
           for (int j = 0; j < inputsNumber; j++) {
               d1 = inputVect[i][j];
               d1 -= x[j];
               dev += d1 * d1/(2* x[j+inputsNumber]* x[j+inputsNumber]);
           }
           dev = Math.exp(-dev);
           y=dev;
           dev -= target[i];
           dev *= 2;
           for (int j = 0; j < inputsNumber; j++) {
               d1 = x[j+inputsNumber]* x[j+inputsNumber];
               g[j] += dev * y*2*(inputVect[i][j] - x[j]) / (2* d1);
               d1 *=  x[j+inputsNumber];
               g[j+inputsNumber] += dev * y*(inputVect[i][j] - x[j])*(inputVect[i][j] - x[j]) / d1;
           }
       }
        return true;//false;
    }

    public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += "power(" + inputEquation[i] + ((a[i] < 0) ? "+" + Utils.convertDouble(-a[i]) : Utils.convertDouble(-a[i])) + ";2)/power(2*" + Utils.convertDouble(a[inputsNumber + i]) + ";2)" + ((i == inputsNumber - 1) ? "" : "+");
        }
        s = "exp(-(" + s + "))";
        return s;
    }


    @Override
    public Class getConfigClass() {
        return GaussianMultiModelConfig.class;
    }
}