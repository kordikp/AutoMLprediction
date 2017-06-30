/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.trainers.stopping;

/**
 * Implements the Early Stopping Criterion
 */

public class GLaEarlyStopping implements java.io.Serializable, StoppingCriterion {
    private double bestErr;
    private double bestValErr;
    private double minInStrip;
    private double[] lastErr;
    private double actErr;
    private double actValErr;
    private double gl;
    private double ep;
    private int maxEpochs = 30000;
    private int epoch;
    private int alfa = 5;

    public void init(int maxEp) {
        maxEpochs = maxEp;
        lastErr = new double[alfa];
        for (int i = 1; i < alfa; i++) lastErr[i] = Double.MAX_VALUE;
        bestValErr = bestErr = Double.MAX_VALUE;
        epoch = 0;
    }

    public void reset(int maxEp) {
        maxEpochs = maxEp;
        epoch = 0;
        lastErr = new double[alfa];
        for (int i = 1; i < alfa; i++) lastErr[i] = Double.MAX_VALUE;
        bestValErr = bestErr = actErr = actValErr = Double.MAX_VALUE;
    }

    public void storeError(double error) {
        actErr = error;
        epoch++;
        if (actErr < bestErr) bestErr = actErr;
        if (lastErr != null) {
            System.arraycopy(lastErr, 1, lastErr, 0, alfa - 1);
            lastErr[alfa - 1] = actErr;
            minInStrip = actErr;
            for (int i = 0; i < alfa - 1; i++)
                if (lastErr[i] < minInStrip) minInStrip = lastErr[i];
        }
    }

    public void storeValError(double valError) {
        actValErr = valError;
        if (actValErr < bestValErr) bestValErr = actValErr;
        if (bestValErr == 0) bestValErr = 0.000001;
    }

    public double getBestValError() {
        return bestValErr;
    }

    public boolean stop() {
        gl = 100 * (actValErr / bestValErr - 1);
        ep = 0;
        if (lastErr != null) {
            for (int i = 0; i < alfa; i++) ep += lastErr[i];
            ep = 1000 * (ep / ((double) alfa * minInStrip));
        }
        /*System.out.println("je to ep,gl,bestValErr");
        System.out.println(ep);
        System.out.println(gl);
        System.out.println(bestValErr);*/
        if (epoch > maxEpochs) return true;
        return (gl > alfa) && (ep < 1001);
    }
}
