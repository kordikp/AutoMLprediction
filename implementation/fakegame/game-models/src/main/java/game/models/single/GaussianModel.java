package game.models.single;

import game.utils.Utils;
import configuration.models.single.GaussianModelConfig;


/**
 * Gaussian transfer function is detarmined to model hat-like relationships.
 * It also provides the grandient of the error function
 */
public class GaussianModel extends SingleModel {

    public void setInputsNumber(int inputs) {
        coef = inputs+3;
        a = new double [coef];
        gradient = new double [coef];

        super.setInputsNumber(inputs);
    }

    @Override
    public double[] computeStartingPoint() {
        double tmin = Double.MAX_VALUE;
        double tmax = Double.MIN_VALUE;
        int idx_max = 0;
        for (int i = 0; i < learning_vectors; i++) {
            if(target[i]<tmin) tmin = target[i];
            if(target[i]>tmax) {
                tmax = target[i];
                idx_max = i;
            }
        }
        
        double[] sp = new double[coef];
        System.arraycopy(inputVect[idx_max], 0, sp, 0, inputsNumber);
        // center of hat to positive training example
        sp[inputsNumber]=tmin;   // bias
        sp[inputsNumber+1]=+tmax-tmin; // amplitude
        sp[inputsNumber+2]=5*(+tmax-tmin); // large hat
        return sp;
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
        d2 = (1 + a[inputsNumber + 2]);
        d2 *= d2;
        outValue = java.lang.Math.exp(d1 / d2);
        outValue = outValue * (1 + a[inputsNumber + 1]) + a[inputsNumber];
        return outValue;    
    }

    public double getError(double x[]) {
        double dev,d1,d2,err=0;
        for (int i = 0; i < learning_vectors; i++) {
            dev = 0;
            for (int j = 0; j < inputsNumber; j++) {
                d1 = inputVect[i][j];
                d1 -= x[j];
                dev += d1 * d1;
            }
            d1 = -dev;
            d2 = (1 + x[inputsNumber + 2]);
            d2 *= d2;
            dev =  java.lang.Math.exp(d1 / d2);
            dev = dev * (1 + x[inputsNumber + 1]) + x[inputsNumber];
            dev -= target[i];
            err += dev*dev;

        }
   //     String xx="";
   //     for (int i = 0; i < coef; i++) xx += "|"+Utils.convertDouble(x[i]);
   //     xx += "|";
   //     System.out.println(xx+" error: "+err);
         return err;
    }

    public boolean gradient(double x[], double g[]) {
        double dev, d0, d1, d2, d3;
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
            d2 = (1 + x[inputsNumber + 2]);
            d2 *= d2;
            d0 =java.lang.Math.exp(d1 / d2);

            //d0 = getOutput(inputVect[i]);
            //d0=(d0-  x[inputsNumber])/ (1 + x[inputsNumber + 1]);
            d3 = (1 + x[inputsNumber + 1]) * d0;
            dev = x[inputsNumber] + d3;
            dev -= target[i];
            dev *= 2;
            g[inputsNumber] += dev;
            g[inputsNumber + 1] += dev * d0;
            //g[2*inputsNumber + 2] += dev * d3 *  2*(d0-x[inputsNumber + 2]) / d2;
            g[inputsNumber + 2] += dev * d3 * (-2) * (d1 / (d2 * (1 + x[inputsNumber + 2])));
            for (int j = 0; j < inputsNumber; j++) {
                g[j] += dev * d3 * 2 * ((inputVect[i][j] - x[j]) / d2);
                //g[inputsNumber + j] += dev * d3 *  -2*(x[inputsNumber + j]-x[j]*inputVal[i][j]) / d2;
            }
        }
   //     String xx="";
  //      for (int i = 0; i < coef; i++) xx += "|"+Utils.convertDouble(x[i]);
  //      xx += "|";
  //      System.out.println(xx+" gradient g[3]: "+g[3]);
        return true;
    }
     public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += "power(" + inputEquation[i] + ((a[i]<0)?"+" + Utils.convertDouble(-a[i]): Utils.convertDouble(-a[i])) + ";2)"+((i== inputsNumber -1)?"":"+");
        }
        s = Utils.convertDouble(a[inputsNumber + 1] + 1) + "*exp(-(" + s + ")/power(" + Utils.convertDouble(1 + a[inputsNumber + 2]) + ";2)" + ")" + (a[inputsNumber] >= 0 ? "+ " : "")  + Utils.convertDouble(a[inputsNumber]);
        return s;
    }

    @Override
    public Class getConfigClass() {
        return GaussianModelConfig.class;
    }
}