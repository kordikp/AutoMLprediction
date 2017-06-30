package game.models.single;

import game.utils.Utils;
import configuration.models.single.SigmoidModelConfig;

/**
 * SigmoidModel allows to build model with logistic transfer function. Coeficients are estimated by standard mechanism.
 * It also provides the grandient of the error function
 */
public class SigmoidModel extends SingleModel {

    public void setInputsNumber(int inputs) {
        coef = inputs + 3;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double outValue = a[inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i] * input_vector[i];
        outValue = java.lang.Math.exp(-outValue);
        outValue++;
        outValue = 1 / outValue;
        outValue = a[inputsNumber + 2] + a[inputsNumber + 1] * outValue;
        return outValue;
    }

    /**
       * Provides (in g) gradient of the energy function in the position given by x
       */
      public boolean gradient(double x[], double g[]) {
          double dev, d0,e;
          for (int j = 0; j < coef; j++) g[j] = 0;
          for (int i = 0; i < learning_vectors; i++) {
              //if (validationEnabled) if (inValidationSet[i]) continue;
              dev = x[inputsNumber];
              for (int j = 0; j < inputsNumber; j++)
                  dev += inputVect[i][j] * x[j];
              dev = java.lang.Math.exp(-dev);
              e = dev;
              dev++;
              dev = 1 / dev;
              d0 = dev;
              dev =  x[inputsNumber + 2] + x[inputsNumber + 1] * dev;
              dev -= target[i];
              dev *= 2;
              g[inputsNumber +2] += dev;
              g[inputsNumber +1] += dev*d0;
              dev *=  x[inputsNumber + 1] *e / ((1 + e)*(1 + e));
              for (int j = 0; j < inputsNumber; j++) g[j] += dev * inputVect[i][j];
              g[inputsNumber] += dev;

          }
          return true;
      }
     public double getError(double x[]) {
          double dev,err=0;
         for (int i = 0; i < learning_vectors; i++) {
              dev = x[inputsNumber];
              for (int j = 0; j < inputsNumber; j++)
                  dev += inputVect[i][j] * x[j];
              dev = java.lang.Math.exp(-dev);
              dev++;
              dev = 1 / dev;
              dev =  x[inputsNumber + 2] + x[inputsNumber + 1] * dev;
              dev -= target[i];
              err += dev*dev;

          }
          return err;
      }    /*
    public boolean computeErrorAndGradient(double x[]) {
 /*       error = 0;
        double dev, y;
        System.arraycopy(x,0,a,0,coef);
          for (int j = 0; j < coef; j++) gradient[j] = 0;
          for (int i = 0; i < learning_vectors; i++) {
              y = getOutput(inputVect[i]);
              dev = y - target[i];
              error += dev;
              dev *= 2;
              gradient[inputsNumber +2] += dev;
              dev *= y * (1 - y);
              gradient[inputsNumber] += dev;
              for (int j = 0; j < inputsNumber; j++) gradient[j] += dev * inputVect[i][j];
              gradient[inputsNumber +1]=dev*(y/a[inputsNumber +1]-a[inputsNumber +2]); //?
          }
          return false;
    }*/
     public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += Utils.convertDouble(a[i]) + "* " + inputEquation[i] + (a[i + 1] >= 0 ? " + " : " ");
        }
        s += Utils.convertDouble(a[inputsNumber]);
        s =  Utils.convertDouble(a[inputsNumber +1])+"/(1+exp(-(" + s + ")))+"+ Utils.convertDouble(a[inputsNumber +2]);
        return s;
    }

    @Override
    public Class getConfigClass() {
        return SigmoidModelConfig.class;
    }
}