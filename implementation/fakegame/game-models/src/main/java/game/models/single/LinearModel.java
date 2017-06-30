package game.models.single;

import configuration.models.ModelConfig;
import configuration.models.single.LinearModelConfig;
import Jama.Matrix;
/**
 * LinearModel allows to build model with linear transfer function. Coeficients are estimated by LMS method.
 * It also provides the grandient of the error function
 */
public class LinearModel extends SingleModel {
    boolean retrainWhenLMSfails;

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        retrainWhenLMSfails = ((LinearModelConfig)cfg).getRetrainWhenLmsFails();
    }

    public void setInputsNumber(int inputs) {
        coef = inputs +1;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    public void learn() {
        learned=false;
        if(estimateCoefficientsUsingLMSMethod()) learned=true;
        else if(retrainWhenLMSfails) {
            super.learn();  //use default optimization procedure
            learned=true;
        }
    }

    boolean estimateCoefficientsUsingLMSMethod() {
        double[][] x = new double[learning_vectors][coef];
        double[][] y = new double[learning_vectors][1];
        double[][] aa;

        for (int j = 0; j < learning_vectors; j++) x[j][coef-1] = 1;
        for (int i = 0; i < coef-1; i++) {
            for (int j = 0; j < learning_vectors; j++) {
                    x[j][i] = inputVect[j][i];
            }
        }
        for (int j = 0; j < learning_vectors; j++) y[j][0] = target[j];

        Matrix xx = new Matrix(x);
        Matrix xt = xx.transpose();

        Matrix yy = new Matrix(y);
        xx = xt.times(xx); /////x=xT*x
        if(xx.det()==0) return false; // matrix is singular !!! cannot be inverted
        try {
                  xx = xx.inverse();                  /////power((xT*x):-1)
                  } catch (Exception e) {return false;}
        
        yy = xt.times(yy); //// xT*y
        Matrix res = xx.times(yy);///a=(power((xT*x):-1))*xT*y

        aa = res.getArray(); //back to array of doubles
        for (int i = 0; i < coef; i++) {
            a[i]=aa[i][0];
        }

       return true;
    }

    @Override
       protected double getOutputWith(double[] input_vector, double[] a) {
        double outValue = a[inputsNumber];
        for (int i = 0; i < inputsNumber; i++) outValue += a[i] * input_vector[i];
        return outValue;
    }


    @Override
    public Class getConfigClass() {
       return LinearModelConfig.class;
    }

    public boolean computeErrorAndGradient(double x[]) {
        double dev;
        error = 0;
        System.arraycopy(x,0,a,0,coef);
        for (int j = 0; j < coef; j++) gradient[j] = 0;
        for (int i = 0; i < learning_vectors; i++) {
            dev = getOutput(inputVect[i])- target[i];
            error += dev * dev;
            dev *= 2;
            for (int j = 0; j < coef - 1; j++) gradient[j] += dev * inputVect[i][j];
            gradient[coef - 1] += dev;
        }
        return true;
    }

    public boolean gradient(double x[], double g[]) {
        double dev;
        if(a==null)return false;
        System.arraycopy(x,0,a,0,coef);
        for (int j = 0; j < coef; j++) g[j] = 0;
        for (int i = 0; i < learning_vectors; i++) {
            dev = getOutput(inputVect[i])- target[i];
            dev *= 2;
            for (int j = 0; j < coef - 1; j++) g[j] += dev * inputVect[j][i];
            g[coef - 1] += dev;
        }
        return true;
    }

    public double getError(double x[]) {
          double dev,err=0;
         for (int i = 0; i < learning_vectors; i++) {
              dev = x[inputsNumber];
              for (int j = 0; j < inputsNumber; j++)
                  dev += inputVect[i][j] * x[j];
              dev -= target[i];
              err += dev*dev;
          }
          return err;
    }

    public String toEquation(String[] inputEquation) {
        String outValue = Double.toString(a[inputsNumber]);
        for (int i = 0; i < inputsNumber; i++) {
            //simplify
            if (a[i] != 0.0) {
                outValue = a[i] + "*" + inputEquation[i] + (outValue.startsWith("-") ? outValue : "+" + outValue);
            }
        }
        return outValue;
    }
}
