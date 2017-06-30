package game.trainers;

import game.configuration.Configurable;

import java.util.Vector;


/**
 * this class defines functions that has to implement every training algorithm
 * that is designed to train units of the GMDH network
 */
public class Trainer implements Configurable {

    Vector<double[]> iterationHistory;
    Vector<double[]> errorHistory;
    boolean logError;
    boolean logIterations;
    transient GradientTrainable unit;
    int coefficients;
    double[] best;
    double[] startingPoint;
    double errorBestSoFar;
    private double firstError, lastError;
    private int cnt;
    private boolean neuron = false;

    public Trainer() {
        errorBestSoFar = Double.MAX_VALUE;
        this.setLogError(false);
        this.setLogIterations(false);
        iterationHistory = new Vector<double[]>();
        errorHistory = new Vector<double[]>();
    }

    public boolean isLogError() {
        return logError;
    }

    public void setLogError(boolean logError) {
        this.logError = logError;
    }

    public boolean isLogIterations() {
        return logIterations;
    }

    public void setLogIterations(boolean logIterations) {
        this.logIterations = logIterations;
    }

    public double[] getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(double[] startingPoint) {
        this.startingPoint = startingPoint;
    }

    public void init(GradientTrainable uni, Object cfg, int coef) {
        init(uni, cfg);
        setCoef(coef);
    }

    /**
     * initializes the training algorithm
     *
     * @param uni unit to be trained
     * @param cfg Configuration bean.
     */
    public void init(GradientTrainable uni, Object cfg) {
        unit = uni;
        //true if unit is instance of neuron, false otherwise
        //TODO
        // neuron = unit instanceof Neuron;
    }

    /**
     * Performs initialization of variables and structures based on coef.
     *
     * @param coef Input coefficient.
     */
    public void setCoef(int coef) {
        best = new double[coef];
        coefficients = coef;
    }

    /**
     * starts the teaching process
     */
    public void teach() {
    }

    /**
     * returns the best configuration so far
     */
    public double[] getBest() {
        return best;
    }

    /**
     * returns the ith element of the best configuration so far
     *
     * @param index index
     */
    public double getBest(int index) {
        return best[index];
    }

    public void setBestForever(double[] x) {
        System.arraycopy(x, 0, best, 0, coefficients);
        errorBestSoFar = Double.MIN_VALUE;
    }

    public double getError(double[] x) {
        double errLearning = unit.getError(x);
        double errValidation = unit.getValidationError(x);
        if (logIterations) iterationHistory.add(x);
        if (logError) {
            double[] error = new double[2];
            error[0] = errLearning;
            error[1] = errValidation;
            errorHistory.add(error);
        }
        if (errValidation > 0) {  //when Validation supported
            if (errValidation < errorBestSoFar) {
                System.arraycopy(x, 0, best, 0, coefficients);
                errorBestSoFar = errValidation;
            }
        } else if (errLearning < errorBestSoFar) {
            System.arraycopy(x, 0, best, 0, coefficients);
            errorBestSoFar = errLearning;
        }
        return errLearning; // correct indexes (starting from zero)
    }

    public double[][] scanErrorSurface(int idX, int idY, int resolution, double scale) {
        double[][] data = new double[resolution][resolution];
        double[] x = new double[coefficients];
        for (int k = 0; k < coefficients; k++) x[k] = best[k];
        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                x[idX] = ((double) i / resolution - 0.5) * scale;
                x[idY] = ((double) j / resolution - 0.5) * scale;
                data[i][j] = unit.getError(x);
            }
        }
        return data;
    }

    public double[][] scanErrorSurfaceHistory(int idX, int idY, int resolution, double scale, int index) {
        double[][] data = new double[resolution][resolution];
        double[] x = new double[coefficients];
        for (int k = 0; k < coefficients; k++) x[k] = iterationHistory.get(index)[k];
        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                x[idX] = ((double) i / resolution - 0.5) * scale;
                x[idY] = ((double) j / resolution - 0.5) * scale;
                data[i][j] = unit.getError(x);
            }
        }
        return data;
    }

    /**
     * Computes the error and records it to plot in in the graphcanvas
     *
     * @param x           input vector
     * @param record      every "record" call, the error is recorded
     * @param redraw      every "redraw" call, the graph is redrawed
     * @param setProgress
     * @return RMS error
     */
    public double getAndRecordError(double[] x, int record, int redraw, boolean setProgress) {
        double error = getError(x);
        if (neuron) {
            cnt++;
            if (lastError > 0) {
                if (error < lastError) {
                    if ((cnt % record) == 0) {
                        //TODO
                        //((Neuron)unit).addRMSerror(error);
                        lastError = error;
                        //if(setProgress) ((Neuron)unit).setProgress(100 - (int) (100 * (error / firstError)));
                        if ((cnt % redraw) == 0) {
                            //  if (GraphCanvas.getInstance() != null)
                            //      GraphCanvas.getInstance().redraw();
                        }
                    }
                }
            } // for the first time
            else {
                //((Neuron)unit).addRMSerror(error);
                lastError = firstError = error;
            }

        } //consume exception when unit is model

        return error;
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "nothing";
    }

    /**
     * no config class
     */
    public Class getConfigClass() {
        return null;
    }

    public boolean allowedByDefault() {
        return false;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return true;
    }

    public GradientTrainable getUnit() {
        return unit;
    }

    public void setUnit(GradientTrainable unit) {
        this.unit = unit;
    }

    public Vector<double[]> getIterationHistory() {
        return iterationHistory;
    }

    /**
     * Returns the iteration history in certain dimensions x and y - useful for generating 2D plots
     *
     * @param x first dimension
     * @param y second dimension
     * @return time series of iteration steps in the (x,y) domain
     */
    public Vector<double[]> getIterationHistory(int x, int y) {
        Vector<double[]> iterationHistoryXY = new Vector();
        for (double[] ih : iterationHistory) {
            double[] rec = new double[2];
            rec[0] = ih[x];
            rec[1] = ih[y];
            iterationHistoryXY.add(rec);
        }
        double[] bes = new double[2];
        bes[0] = best[x];
        bes[1] = best[y];
        iterationHistoryXY.add(bes);
        return iterationHistoryXY;
    }

    public Vector<double[]> getErrorHistory() {
        return errorHistory;
    }

}