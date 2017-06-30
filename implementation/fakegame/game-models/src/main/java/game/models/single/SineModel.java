package game.models.single;

import game.utils.Utils;
import configuration.models.single.SineModelConfig;

/**
 * SineModel allows to build model with sin transfer function.
 * It also provides the grandient of the error function
 */
public class SineModel extends SingleModel {

    public void setInputsNumber(int inputs) {
        coef = 3*inputs+1;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

     @Override
     public double[] computeStartingPoint() {
        double[] sp = new double[coef];
        for (int i = 0; i < inputsNumber; i++) {
            sp[i] =  rnd.nextDouble()/10;
            sp[i+inputsNumber] = rnd.nextDouble()/10;
            sp[i+2*inputsNumber] = 1;
        }
         sp[3*inputsNumber] = 0;
        return sp;
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double  outValue = a[3*inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i+2*inputsNumber]*java.lang.Math.sin(a[i] * input_vector[i]+a[i+inputsNumber]);
        //outValue = outValue);
        return outValue;
    }

    public boolean gradient(double x[], double g[]) {
         double dev, cosy;
         for (int j = 0; j < coef; j++) gradient[j] = 0;
         for (int i = 0; i < learning_vectors; i++) {
             if (inValidationSet[i]) continue;
             dev =  x[3*inputsNumber];
             for (int j = 0; j < inputsNumber; j++)
                 dev += x[j+2*inputsNumber]*java.lang.Math.sin(inputVect[i][j] * x[j]+x[j+inputsNumber]);
             dev -= target[i];
             dev *= 2;
             g[3*inputsNumber] += dev;
             for (int j = 0; j < inputsNumber; j++) {
                 cosy = x[j+2*inputsNumber]*java.lang.Math.cos(inputVect[i][j] * x[j]+x[j+inputsNumber]);
                 g[j] += dev * cosy * inputVect[i][j];
                 g[j+inputsNumber] += dev * cosy;
                 g[j+2*inputsNumber] += dev * java.lang.Math.sin(inputVect[i][j] * x[j]+x[j+inputsNumber]);
             }
         }  //gradient unfortunately worsens the convergence
         return true;

    }
    public String toEquation(String[] inputEquation) {
        String s = "sin("+Utils.convertDouble(a[0]) + "* " + inputEquation[0] + (a[inputsNumber] >= 0 ? " + " : " ")+Utils.convertDouble(a[inputsNumber])+")";

        for (int i = 1; i < inputsNumber; i++) {
            s += "+sin("+Utils.convertDouble(a[i]) + "* " + inputEquation[i] + (a[i + inputsNumber] >= 0 ? " + " : " ")+Utils.convertDouble(a[i+ inputsNumber])+")";
        }

      return s+(a[2*inputsNumber] >= 0 ? " + " : " ")+Utils.convertDouble(a[2*inputsNumber]);
    }

   
    @Override
    public Class getConfigClass() {
        return SineModelConfig.class;
    }
}