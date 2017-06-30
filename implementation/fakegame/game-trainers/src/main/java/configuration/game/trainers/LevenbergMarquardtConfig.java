/**
 * @author Karel Moulik
 * @version 0.01
 */
package configuration.game.trainers;


import configuration.AbstractCfgBean;

public class LevenbergMarquardtConfig extends AbstractCfgBean {

    private int rec = 10; //record
    private int draw = 100; //redraw

    private double gradStepMult = 1000.0;
    private double hessStepMult = 10000000.0;

    private int iterMax = 200;
    private double tolErr = 0.5;

    public int getRec() {
        return rec;
    }

    public int getDraw() {
        return draw;
    }

    public double getGradStepMult() {
        return gradStepMult;
    }

    public double getHessStepMult() {
        return hessStepMult;
    }

    public int getIterMax() {
        return iterMax;
    }

    public double getTolErr() {
        return tolErr;
    }


}