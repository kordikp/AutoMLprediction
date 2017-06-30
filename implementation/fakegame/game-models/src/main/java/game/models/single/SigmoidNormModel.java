package game.models.single;

import game.utils.Utils;
import configuration.models.single.SigmoidNormModelConfig;


/**
 * SigmoidModel allows to build model with logistic transfer function. Coeficients are estimated by standard mechanism.
 * It also provides the grandient of the error function
 */
public class SigmoidNormModel extends SingleModel {

    public void setInputsNumber(int inputs) {
        coef = inputs + 1;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double outValue = a[inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i] * input_vector[i];
        outValue = Math.exp(-outValue);
        outValue++;
        outValue = 1 / outValue;
        return outValue;
    }
        /*
     public synchronized double getError(double x[]) {
        double error = 0;
        double dev;
        double [] aback = new double[coef];
         System.arraycopy(a,0,aback,0,coef); //backup coefficients
        System.arraycopy(x,0,a,0,coef); //replace coefficients by parameters x
        for (int i = 0; i < learning_vectors; i++) {

            dev = getOutput(inputVect[i])- target[i];
            error += dev * dev;
        }
  //      for (int i = 0; i < coef; i++) System.out.print(x[i] +" ");
  //           System.out.println();
   //         System.out.println(Double.valueOf(error));
         System.arraycopy(aback,0,a,0,coef); //restore original coefficients

        return error;
    }

    public boolean hessian(double x[], double h[][]) {
          double dev, y;
          System.arraycopy(x,0,a,0,coef);
          for (int j = 0; j < coef; j++)
              for (int k = 0; k < coef; k++) h[j][k] = 0;

          for (int i = 0; i < learning_vectors; i++) {
              y = dev = getOutput(inputVect[i]);
              dev -= target[i];
              dev *= 2;
              dev *= y * (1 - y);
              //todo not finished yet
       //       for (int j = 0; j < coef - 1; j++) g[j] += dev * inputVal[i][j];
       //       g[coef - 1] += dev;
          }
          return false;
      }

      /**
       * Provides (in g) gradient of the energy function in the position given by x
       */
      public boolean gradient(double x[], double g[]) {
          double dev, y;
          for (int j = 0; j < coef; j++) g[j] = 0;
          for (int i = 0; i < learning_vectors; i++) {
              if (validationEnabled) if (inValidationSet[i]) continue;
              y = getOutputWith(inputVect[i],x);
              dev = y - target[i];
              dev *= 2;
              dev *= y * (1 - y);
              for (int j = 0; j < coef - 1; j++) g[j] += dev * inputVect[i][j];
              g[coef - 1] += dev;
          }
          return true;
      }
            /*

    public boolean computeErrorAndGradient(double x[]) {
        error = 0;
        double dev, y;
        System.arraycopy(x,0,a,0,coef);
          for (int j = 0; j < coef; j++) gradient[j] = 0;
          for (int i = 0; i < learning_vectors; i++) {
              y = getOutput(inputVect[i]);
              dev = y - target[i];
              error += dev;
              dev *= 2;
              dev *= y * (1 - y);
              for (int j = 0; j < coef - 1; j++) gradient[j] += dev * inputVect[i][j];
              gradient[coef - 1] += dev;
          }
          return true;
    }        */
     public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += Utils.convertDouble(a[i]) + "* " + inputEquation[i] + (a[i + 1] >= 0 ? " + " : " ");
        }
        s += Utils.convertDouble(a[inputsNumber]);
        s = "1/(1+exp(-(" + s + ")))";
        return s;
    }

   
    @Override
    public Class getConfigClass() {
        return SigmoidNormModelConfig.class;
    }
}