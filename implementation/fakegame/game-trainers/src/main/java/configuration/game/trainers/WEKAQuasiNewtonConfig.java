package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class WEKAQuasiNewtonConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;
    private double epsilon;
    private int maxIterations;

    /**
     * inicialises parametres to its default values
     */
    public WEKAQuasiNewtonConfig() {
        epsilon = 1.4901161193847656E-8;
        maxIterations = 200;
        //epsilon = 1.12e-16;
    }

    /**
     * function to pass the values of parameters to the unit
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * function to pass the values of parameters to the unit
     */
    public int getMaxItetations() {
        return maxIterations;
    }
}
