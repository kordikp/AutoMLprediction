package game.trainers;


/**
 * This interface sumarize methods needed for training by gradient methods
 */
public interface GradientTrainable {
    public Trainer getTrainer();

    /**
     * @param myTrainer sets the training method for the model
     */
    public void setTrainer(Trainer myTrainer);

    /**
     * computes the error of the unit on the training set
     *
     * @param x coeficients estimated by training method
     * @return error of the model with coefficients a set to x
     */
    public double getError(double[] x);

    /**
     * computes the gradient of the error function
     *
     * @param x coeficients estimated by training method
     * @param g gradient vector
     * @return true when implemented
     */
    public boolean gradient(double[] x, double[] g);

    /**
     * computes the matrix of second derivations the error function
     *
     * @param x coeficients estimated by training method
     * @param h hessian matrix
     * @return true when implemented
     */
    public boolean hessian(double[] x, double[][] h);

    /**
     * Computes error and gradinent in one loop - saves half calls of the getOutput() function
     *
     * @param x coeficients estimated by training method
     * @return true when implemented
     */
    public boolean computeErrorAndGradient(double x[]);

    /**
     * gets the gradient vector computed by the computeErrorAndGradient() function
     *
     * @return gradient computed by computeErrorAndGradient
     */
    public double[] getGradient();

    /**
     * gets the error computed by the computeErrorAndGradient() function
     *
     * @return error computed by computeErrorAndGradient
     */
    @Deprecated
    public double getError();

    /**
     * computes the error of the unit on the validation set
     *
     * @param x coeficients estimated by training method
     * @return error of the model with coefficients a set to x
     * when Validation set is not supported, returns -1
     */
    double getValidationError(double[] x);
}
