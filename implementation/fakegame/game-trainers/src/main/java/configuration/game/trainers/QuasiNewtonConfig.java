/**
 * @author Pavel Kordik
 * @version 0.90
 */
package configuration.game.trainers;

import configuration.AbstractCfgBean;

/**
 * Class for the IterPolyNeuron unit configuration
 */
public class QuasiNewtonConfig extends AbstractCfgBean {
    private int rec = 10;
    private int draw = 10;
    private boolean forceAnalyticHessian = false;


    public int getDraw() {
        return draw;
    }

    public int getRec() {
        return rec;
    }

    public boolean isForceAnalyticHessian() {
        return forceAnalyticHessian;
    }
}
