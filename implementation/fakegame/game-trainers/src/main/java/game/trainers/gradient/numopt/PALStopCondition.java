package game.trainers.gradient.numopt;

import common.MachineAccuracy;

/**
 * User: honza
 * Date: 21.2.2007
 * Time: 22:42:16
 */
public class PALStopCondition extends StopCondition {
    private double tolerancex;
    private double tolerancefx;
    private int numFuncStops;

    private int countFuncStops;

    private double prevfx;
    private double[] prevx;


    public PALStopCondition() {
        this(MachineAccuracy.EPSILON);
    }

    public PALStopCondition(double otolerance) {
        this(otolerance, otolerance, 4);
    }

    private PALStopCondition(double tolerancex, double tolerancefx, int numFuncStops) {
        this.tolerancex = tolerancex;
        this.tolerancefx = tolerancefx;
        this.numFuncStops = numFuncStops;
    }

    public void init(double ofx, double[] ox) {
        prevfx = ofx;
        prevx = ox.clone();
        countFuncStops = 0;
    }

    public boolean stop(double ofx, double[] ox) {
        boolean stop = true;

        // check function argument for stop
        for (int i = 0; i < ox.length; i++) {
            if (Math.abs(ox[i] - prevx[i]) > tolerancex) {
                stop = false;
                break;
            }
//            System.out.println("Math.abs(ox[i] - prevx[i])<= tolerancex");
//            System.out.println("ox[i] = " + ox[i]);
//            System.out.println("prevx[i] = " + prevx[i]);
        }
//        System.out.println("stopX: " + stop);
//        System.out.println("Math.abs(ofx - prevfx) <= tolerancefx = " + (Math.abs(ofx - prevfx) <= tolerancefx));
//        System.out.println("countFuncStops = " + countFuncStops);

        if (!stop) {
            if (Math.abs(ofx - prevfx) <= tolerancefx) { // check function value for stop
                countFuncStops++;
            } else {
                countFuncStops = 0;
            }

            if (countFuncStops >= numFuncStops) {
                stop = true;
//                System.out.println("countFuncStops >= numFuncStops");
            }
        }

        if (!stop) {
            prevfx = ofx;
            prevx = ox.clone();
        }

        return stop;
    }
}
