package game.trainers;

import game.trainers.gradient.Newton.Uncmin_methods;
import game.trainers.pso.Hejno;
import configuration.game.trainers.PSOConfig;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PSOTrainer extends Trainer implements Uncmin_methods {
    int cnt;

    /**
     * starts the teaching process
     */
    public void teach() {
        new Hejno(this, coefficients, 5.0d);
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "Particle Swarm Optimization - old implementation";
    }

    public double f_to_minimize(double[] x) {
        return getAndRecordError(x, 10, 100, true);
    }

    public void gradient(double[] x, double[] g) {
        unit.gradient(x, g);
    }

    public void hessian(double[] x, double[][] h) {
        unit.hessian(x, h);
    }

    public boolean allowedByDefault() {
        return false;
    }

    public Class getConfigClass() {
        return PSOConfig.class;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return false;
    }
}
