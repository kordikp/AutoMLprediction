package game.models.single;

import game.utils.Utils;
import configuration.models.single.SineNormModelConfig;


/**
 * SineModel allows to build model with sin transfer function.
 * It also provides the grandient of the error function
 */
public class SineNormModel extends SingleModel {

    public void setInputsNumber(int inputs) {
        coef = inputs + 1;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double  outValue = a[inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i] * input_vector[i];
        outValue = Math.sin(outValue);
        return outValue;
    }
    public double[] computeStartingPoint() {
         double[] sp = new double[coef];
         for (int i = 0; i < coef; i++)
              sp[i] =  rnd.nextDouble()/10;// generate new random coefficients
        return sp;
     }


    public boolean computeErrorAndGradient(double x[]) {
        error = 0;
        double dev, cosy, d0;
        for (int j = 0; j < coef; j++) gradient[j] = 0;
        for (int i = 0; i < learning_vectors; i++) {
            dev = x[inputsNumber];
            for (int j = 0; j < inputsNumber; j++)
                dev += inputVect[i][j] * x[j];
            d0 = dev;
            dev = Math.sin(dev);
            cosy =  Math.cos(d0);
            dev -= target[i];
            error += dev*dev;
            dev *= 2;
            gradient[inputsNumber] += dev * cosy;
            for (int j = 0; j < inputsNumber; j++) gradient[j] += dev * cosy * inputVect[i][j];
        }
        return true;

    }


    public boolean gradient(double x[], double g[]) {
        error = 0;
         double dev, cosy, d0;
         for (int j = 0; j < coef; j++) gradient[j] = 0;
         for (int i = 0; i < learning_vectors; i++) {
               if (validationEnabled) if (inValidationSet[i]) continue;
             dev = x[inputsNumber];
             for (int j = 0; j < inputsNumber; j++)
                 dev += inputVect[i][j] * x[j];
             d0 = dev;
             dev =Math.sin(dev);
             cosy = Math.cos(d0);
             dev -= target[i];
             dev *= 2;
             g[inputsNumber] += dev * cosy;
             for (int j = 0; j < inputsNumber; j++) g[j] += dev * cosy * inputVect[i][j];
         }
         return true;
    }
      public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += Utils.convertDouble(a[i]) + "* " + inputEquation[i] + (a[i + 1] >= 0 ? " + " : " ");
        }
        s += Utils.convertDouble(a[inputsNumber]);
        s = "sin("+ s + ")";
        return s;
    }

    @Override
    public Class getConfigClass() {
        return SineNormModelConfig.class;
    }
}