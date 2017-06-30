package game.models.single;

import game.models.ModelLearnableBase;
import game.trainers.GradientTrainable;
import game.trainers.Trainer;
import game.utils.GlobalRandom;

import java.util.Random;

import configuration.models.ModelConfig;
import configuration.models.TrainerSelectable;
import configuration.models.single.ModelSingleConfigBase;

/**
 * This is a template class for all simple trainable models.
 */
abstract public class SingleModel extends ModelLearnableBase implements GradientTrainable {
    protected double[] a;
    transient protected double[] gradient;  //gradient
    transient protected double error;
    protected int coef;

    transient boolean[] inValidationSet;
    int validationPercent;
    boolean validationEnabled;
    transient Random rnd;


    transient protected Trainer trainer;

    public void init(ModelConfig cfgi) {
        try {
            TrainerSelectable cfg = (TrainerSelectable) cfgi;
            trainer = (Trainer) cfg.getTrainerClass().newInstance();
            super.init(cfg);
            if (cfg.getTrainerCfg() == null) {
                trainer.init(this, trainer.getConfigClass().newInstance());
            } else
                trainer.init(this, cfg.getTrainerCfg());
            trainer.setCoef(coef);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        ModelSingleConfigBase cfg = (ModelSingleConfigBase) cfgi;
        validationPercent = cfg.getValidationPercent();
        validationEnabled = cfg.isValidationEnabled();
        rnd = new Random();
    }

    public void setInputsNumber(int inputs) {
        super.setInputsNumber(inputs);
        if(trainer!=null)trainer.setCoef(coef);
    }

    public Trainer getTrainer() {
        return trainer;
    }

    /**
     * @param myTrainer sets the training method for the model
     */
    public void setTrainer(Trainer myTrainer) {
        trainer = myTrainer;
        if (trainer != null) trainedBy = trainer.getMethodName();
    }

    public double[] computeStartingPoint() {
        double[] sp = new double[coef];
        for (int i = 0; i < coef; i++)
            sp[i] = GlobalRandom.getInstance().getSmallDouble();// generate new random coefficients
        return sp;
    }


    public void learn() {
        if (validationEnabled)initializeValidationSet();
        trainer.setStartingPoint(computeStartingPoint());
        trainer.teach();
        for (int i = 0; i < coef; i++) {
            double b = trainer.getBest(i);
            if (!Double.isNaN(b) && !Double.isInfinite(b)) a[i] = b;
        }
        learned = true;
    }

    public void initializeValidationSet() {
            inValidationSet = new boolean[learning_vectors];
            for (int i = 0; i < learning_vectors; i++) inValidationSet[i] = false;
            int vectors = (int) (learning_vectors * (validationPercent / 100.0));
            for (int i = 0; i < vectors; i++) setRandomValidationVector();
    }

    private void setRandomValidationVector() {
        int myRand = rnd.nextInt(learning_vectors);
        if (inValidationSet[myRand]) {
            int frst = myRand;
            inValidationSet[frst] = false; // just one round if all generated
            do {
                myRand = (myRand + 1) % learning_vectors; // find first not generated
            } while (inValidationSet[myRand]);
            inValidationSet[frst] = true; // set it back
        }
        inValidationSet[myRand] = true; // mark it generated
    }

    /**
     * Function computes output of the model
     *
     * @param input_vector Specify inputs to the model
     * @return linear combination of inputs
     */
    public double getOutput(double[] input_vector) {
        return getOutputWith(input_vector,a);
    }
     /**
     * Function should be overiden
     */
    protected double getOutputWith(double[] input_vector, double[] a) {
        return Double.NaN;
    }

    public double getOutputTo(int inputVectorIndex) {
        return getOutput(inputVect[inputVectorIndex]);
    }

    public boolean computeErrorAndGradient(double x[]) {
        return false;
    }

    public double[] getGradient() {
        return gradient;
    }

    public double getError() {
        return error;
    }


    synchronized public double getTrainingOrValidationError(double x[], boolean validation) {
        double error = 0, dev;
        if (!validationEnabled) if (validation) return -1;
        for (int i = 0; i < learning_vectors; i++) {
            if (validationEnabled) if (inValidationSet[i] != validation) continue;

            dev = getOutputWith(inputVect[i],x);
            dev -= target[i];
            error += dev * dev;
        }

        return error;
    }

    public double getError(double x[]) {
        return getTrainingOrValidationError(x, false);
    }

    public double getValidationError(double x[]) {
        return getTrainingOrValidationError(x, true);
    }

    /*
        public double getError(double x[]) {
            double error = 0;
            double dev;
            System.arraycopy(x, 0, a, 0, coef);
            for (int i = 0; i < learning_vectors; i++) {
                dev = getOutput(inputVect[i]) - target[i];
                error += dev * dev;
            }
         //   System.out.println(this.toString()+"  "+error);
            return error;
        }
    */

    public boolean hessian(double x[], double h[][]) {
        return false;
    }

    public boolean gradient(double x[], double g[]) {
        return false;
    }

    public void deleteLearningVectors() {
        gradient=null;
        super.deleteLearningVectors();
    }

    public String toString() {
        String outValue = "";
        for (int i = 0; i < coef; i++) outValue += "|" + a[i];
        outValue += "|";
        return outValue;
    }
}