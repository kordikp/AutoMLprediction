/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.trainers.stopping;

/**
 * Implements the Early Stopping Criterion
 */

public class ValMinEarlyStopping implements java.io.Serializable, StoppingCriterion {
    private double bestErr;
    private double bestValErr;
    double[] lastErr;
    private double actErr;
    private double actValErr;
    double gl, ep;
    private int maxEpochs = 1000;
    private int delay;
    private int epoch;
    private int alfa = 5;

    public void init(int maxEp) {
        maxEpochs = maxEp;
        bestValErr = bestErr = Double.MAX_VALUE;
        epoch = 0;
        delay = 0;
    }

    public void reset(int maxEp) {
        maxEpochs = maxEp;
        epoch = 0;
        delay = 0;
        bestValErr = bestErr = actErr = actValErr = Double.MAX_VALUE;
    }

    public void storeError(double error) {
        actErr = error;
        epoch++;
        if (actErr < bestErr) bestErr = actErr;

    }

    public void storeValError(double valError) {
        actValErr = valError;
        if (actValErr < bestValErr) {
            bestValErr = actValErr;
            delay = 0;
        } else delay++;
    }

    public double getBestValError() {
        return bestValErr;
    }

    public boolean stop() {
        //if(delay>alfa)System.out.println(actValErr-bestValErr);
        return delay > alfa;
    }

    public ValMinEarlyStopping clone() throws CloneNotSupportedException {
        super.clone();
        ValMinEarlyStopping c = new ValMinEarlyStopping();
        c.alfa = this.alfa;
        return c;
    }
}
