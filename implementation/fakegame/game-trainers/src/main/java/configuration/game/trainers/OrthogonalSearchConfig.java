package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class OrthogonalSearchConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;
    private double tolfx;
    private double tolx;

    /**
     * inicialises parametres to its default values
     */
    public OrthogonalSearchConfig() {
        tolx = 0.1;
        tolfx = 0.1;
    }

    /**
     * function to pass the values of parameters to the unit
     */
    public double getTolx() {
        return tolx;
    }

    /**
     * function to pass the values of parameters to the unit
     */
    public double getTolfx() {
        return tolfx;
    }
}
