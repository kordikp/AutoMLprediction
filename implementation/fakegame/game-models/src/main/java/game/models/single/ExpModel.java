/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.models.single;

import game.utils.Utils;
import configuration.models.single.ExpModelConfig;

/**
 * this model has n inputs and it's transfer function is exponential.
 */
public class ExpModel extends SingleModel {

    @Override
        public double[] computeStartingPoint() {           
           double[] sp = super.computeStartingPoint();
           sp[inputsNumber+1] = 1;
           sp[inputsNumber+2] = 0;
           sp[inputsNumber+3] = 1;
           return sp;
       }

    public void setInputsNumber(int inputs) {
        coef = inputs + 4;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double outValue = a[inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i] * input_vector[i];
        outValue = Math.exp(outValue * a[inputsNumber + 3]) * a[inputsNumber + 1] + a[inputsNumber + 2];
        return outValue;
    }

    /**
       * Provides (in g) gradient of the energy function in the position given by x
       */
      public boolean gradient(double x[], double g[]) {
          double dev, d1, d0;
        for (int j = 0; j < coef; j++) g[j] = 0;
        for (int i = 0; i < learning_vectors; i++) {
            if (inValidationSet[i]) continue;
            dev = x[inputsNumber];
            for (int j = 0; j < inputsNumber; j++)
                dev += inputVect[i][j] * x[j];
            d0 = dev;
            d1 = Math.exp(dev * x[inputsNumber + 3]);
            dev = d1 * x[inputsNumber + 1] + x[inputsNumber + 2];
            dev -= target[i];
            dev *= 2;
            g[inputsNumber + 2] += dev;
            g[inputsNumber + 1] += dev * d1;
            g[inputsNumber + 3] += dev * d1 * x[inputsNumber + 1] * d0;
            g[inputsNumber] += dev * d1 * x[inputsNumber + 1] * x[inputsNumber + 3];
            for (int j = 0; j < inputsNumber; j++) g[j] += dev * d1 * x[inputsNumber + 1] * x[inputsNumber + 3] * inputVect[i][j];
        }
          return true;
      }

     public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += Utils.convertDouble(a[i]) + "*" + inputEquation[i] + (a[i + 1] >= 0 ? "+" : "");
        }
        s += Utils.convertDouble(a[inputsNumber]);

         s = "exp((" + s +")*"+ Utils.convertDouble(a[inputsNumber + 3])+ ")*"+ Utils.convertDouble(a[inputsNumber +1])+(a[inputsNumber +2] >= 0 ? " + " : " ")+ Utils.convertDouble(a[inputsNumber +2]);
        return s;
    }


    @Override
    public Class getConfigClass() {
        return ExpModelConfig.class;
    }

}