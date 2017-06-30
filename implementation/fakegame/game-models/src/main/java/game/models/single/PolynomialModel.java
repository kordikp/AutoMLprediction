package game.models.single;


import game.models.Element;
import game.trainers.GradientTrainable;
import game.utils.GlobalRandom;
import game.utils.MyRandom;
import game.utils.Utils;

import Jama.Matrix;
import java.util.ArrayList;


import configuration.models.ModelConfig;
import configuration.models.single.PolynomialModelConfig;


/**
 * PolynomialModel allows to build model with randomly initialized polynomial transfer function. Coeficients are estimated by LMS method.
 * It also provides the grandient of the error function
 */
public class PolynomialModel extends SingleModel {
    protected ArrayList elements;
    protected double bias;
    protected transient double [][] elemVal; //to store precomputed elements during training
    protected int maxdeg;

    public void init(ModelConfig cfg) {
       PolynomialModelConfig cf =(PolynomialModelConfig)cfg;
       maxdeg = cf.getMaxDegree();
       super.init(cfg);
    }

     public int getCoefsNumber() {
         return elements.size() + 1;
     }
     protected double getOutputWith(double[] input_vector, double[] a) {
                int elm = elements.size();
         double outValue = a[elm];
         for (int j = 0; j < elm; j++) {
             double val = 1;
             boolean some = false;
             for (int i = 0; i < inputsNumber; i++) {
                 int num = ((Element) elements.get(j)).getIndex(i);
                 if (num > 0) {
                     for (int k = 0; k < num; k++) {
                         some = true;
                         val *= input_vector[i];
                     }
                 }
             }
             outValue += some ? a[j] * val : 0;
         }
         return outValue;
    }
     public double getOutput(double [] input_vector) {
         int elm = elements.size();
         double outValue = bias;
         for (int j = 0; j < elm; j++) {
             double a = ((Element) elements.get(j)).getCoefficent();
             double val = 1;
             boolean some = false;
             for (int i = 0; i < inputsNumber; i++) {
                 int num = ((Element) elements.get(j)).getIndex(i);
                 if (num > 0) {
                     for (int k = 0; k < num; k++) {
                         some = true;
                         val *= input_vector[i];
                     }
                 }
             }
             outValue += some ? a * val : 0;
         }
         return outValue;
     }


     /**
      * coefficients are modified according to the minimalization of the deviation from the learning set
      */

     public void learn() {
         precomputeElements();

         if (!estimateCoefficientsUsingLMSMethod())
            estimateCoefficientsUsingDefaultTrainer(this);
         learned=true;
         deleteLearningVectors();
     }

    public void setInputsNumber(int inputs) {
        MyRandom rnd = GlobalRandom.getInstance();
        int[] degree = new int[inputs];
        int[] enabled = new int[inputs];
        elements = new ArrayList();
        for(int k=1;k<=maxdeg;k++) {
            for(int i=0;i< inputs;i++) {
                for (int j = 0; j < inputs; j++) {
                    if(j==i){
                        enabled[j] = 1;
                        degree[j] = k;
                    } else {
                        enabled[j]=0;
                        degree[j] = 0;
                    }
                }
                elements.add(new Element(rnd.nextDouble() - 0.5, enabled,degree, inputs));
            }
        }
        coef = elements.size() + 1;
        elemVal = new double[maxLearningVectors][coef];

        super.setInputsNumber(inputs);
    }


     void precomputeElements() {
         for (int i = 0; i < learning_vectors; i++)
             for (int j = 0; j < coef - 1; j++) {
                 elemVal[i][j] = getElementValueForVector(i, j);
             }
     }

     double getElementValueForVector(int vector, int elementNumber) {
         boolean some = false;
         double val = 1;
         for (int gg = 0; gg < inputsNumber; gg++) {
             int num = ((Element) elements.get(elementNumber)).getIndex(gg);
             if (num > 0) {
                 some = true;
                 for (int k = 0; k < num; k++) {
                     val *= inputVect[vector][gg];
                 }
             }
         }
         if (some) return val;
         return 0;
     }


     public boolean gradient(double x[], double g[]) {
      double dev;
         if(elemVal==null)return false;
         for (int j = 0; j < coef; j++) {
             g[j] = 0;
         }

         for (int i = 0; i < learning_vectors; i++) {
             if (inValidationSet[i]) continue;
             dev = x[coef - 1];
             for (int j = 0; j < coef - 1; j++) {
                 dev += x[j] * elemVal[i][j];
             }
             dev -= target[i];
             dev *= 2;
             for (int j = 0; j < coef - 1; j++) {
                 g[j] += dev * elemVal[i][j];
             }
             g[coef - 1] += dev;
         }
         return true;
     }

    public double getError(double x[]) {
               double error = 0, pom;
                       for (int i = 0; i < learning_vectors; i++) {
                           if (inValidationSet[i]) continue;
                           pom = x[coef - 1];
                           for (int j = 0; j < coef - 1; j++) {
                               pom += x[j] * elemVal[i][j];
                           }
                           pom -= target[i];
                           error += pom * pom;
                       }
                       return error;

       }

     public double getValidationError(double x[]) {
               double error = 0, pom;
                       for (int i = 0; i < learning_vectors; i++) {
                           if (!inValidationSet[i]) continue;
                           pom = x[coef - 1];
                           for (int j = 0; j < coef - 1; j++) {
                               pom += x[j] * elemVal[i][j];
                           }
                           pom -= target[i];
                           error += pom * pom;
                       }
                       return error;

       }
    
        boolean estimateCoefficientsUsingLMSMethod() {
         int number_of_enabled_coefficients = 0;
         for (Object element1 : elements) {
             if (((Element) element1).isEnabled()) number_of_enabled_coefficients++; //all possible combinations
         }
         double[][] x = new double[learning_vectors][number_of_enabled_coefficients + 1];
         double[][] y = new double[learning_vectors][1];
         double[][] aa;

         for (int j = 0; j < learning_vectors; j++) x[j][0] = 1;
         int newindex = 1;
         for (int i = 0; i < elements.size(); i++) {

             if (((Element) elements.get(i)).isEnabled()) {
                 for (int j = 0; j < learning_vectors; j++) {
                     x[j][newindex] = elemVal[j][i];
                 }
                 newindex++;
             }
         }
         for (int j = 0; j < learning_vectors; j++) y[j][0] = target[j];

         Matrix xx = new Matrix(x);
         Matrix xt = xx.transpose();

         Matrix yy = new Matrix(y);
         xx = xt.times(xx); /////x=xT*x
         if(xx.det()==0) {
               return false;
         }  
            try {
            xx = xx.inverse();                  /////power((xT*x):-1)
            } catch (Exception e) {return false;}
            
         yy = xt.times(yy); //// xT*y
         Matrix res = xx.times(yy);///a=(power((xT*x):-1))*xT*y


         aa = res.getArray(); //back to array of doubles
         newindex = 1;
         for (Object element : elements) {
             if (((Element) element).isEnabled()) {
                 ((Element) element).setCoefficient(aa[newindex++][0]);
             }
         }
         bias = aa[0][0];
        trainedBy="LMS method";
        return true;
     }
     // use default trainer (configured by GAME, in default QuasiNewton method)


       void estimateCoefficientsUsingDefaultTrainer(GradientTrainable me) {
          a = new double[getCoefsNumber()];
   //       trainer = new QuasiNewtonTrainer();
   //       trainer.init(me, new QuasiNewtonConfig());
   //       trainer.setCoef(getCoefsNumber());
   //       trainer.teach();
           //    myNet.myData.paintingRMS = false;
          
          //TODO remove trainers.

           if (validationEnabled)initializeValidationSet();
           
           //TODO tohle jsem zakomentoval nez se rozhodne co s trainers.
           //trainer.setCoef(getCoefsNumber());
           //trainer.setStartingPoint(computeStartingPoint());
           //trainer.teach();
           //for (int i = 0; i < elements.size(); i++) {
            //   if (!Double.isNaN(trainer.getBest(i)) && !Double.isInfinite(trainer.getBest(i))) {
            //       ((Element) elements.get(i)).setCoefficient(trainer.getBest(i));
             //  }
          // }
           //bias = trainer.getBest(elements.size());
       }



     /**
      * returns the transfer function of the unit in the text form
      */

     public String toString() {
         String s = "", m  ;
         boolean some;
         int elm = elements.size();
         for (int j = 0; j < elm; j++) {
             m = Utils.convertDouble(((Element) elements.get(j)).getCoefficent());
             some = false;
             for (int g = 0; g < inputsNumber; g++) {
                 int exp = ((Element) elements.get(j)).getIndex(g);
                 if (exp > 0) {
                     if (!some) s += m;
                     some = true;
                     s += "* (x" + Integer.toString(g) + (exp > 1 ? (")^" + Integer.toString(exp)) : ") ");
                 }
             }
             s = some ? s + " + " : s;
         }
         s += Utils.convertDouble(bias);
         return s;
     }


       public String toEquation(String[] inputEquation) {
        String s = "", m  ;
        boolean some;
        int elm = elements.size();
        for (int j = 0; j < elm; j++) {
            m = Utils.convertDouble(((Element) elements.get(j)).getCoefficent());
            if((j>0)&&((Element) elements.get(j)).getCoefficent()>=0)m ="+"+m;
            some = false;
            for (int g = 0; g < inputsNumber; g++) {
                int exp = ((Element) elements.get(j)).getIndex(g);
                if (exp > 0) {
                    if (!some) s += m;
                    some = true;
                    if(exp > 1) s += "* power(" + inputEquation[g] + ";" + Integer.toString(exp)+")";
                    else s += "* " + inputEquation[g] +  " ";
                }
            }

        }
        s += bias>=0? "+" + Utils.convertDouble(bias): Utils.convertDouble(bias);
        return s;
    }

    public void deleteLearningVectors() {
        elemVal=null;
        a=null;
        super.deleteLearningVectors();
    }

    
    @Override
    public Class getConfigClass() {
        return PolynomialModelConfig.class;
    }
}
