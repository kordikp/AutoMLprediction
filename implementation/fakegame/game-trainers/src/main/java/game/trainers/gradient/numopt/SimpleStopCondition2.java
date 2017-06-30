package game.trainers.gradient.numopt;

/**
 * User: honza
 * Date: 21.2.2007
 * Time: 22:17:46
 * Inspired by Numerical Recipes.
 */
public class SimpleStopCondition2 extends StopCondition {
    private double epsilon = 1.0e-10;
    private double tolerance;
    private int maxRepeats;
//    private ObjectiveFunction func; //TODO not needed now

    private double prevfx;

    private int repeats = 0;


    public SimpleStopCondition2(double otolerance, int omaxRepeats) {
//        this.func = ofunc;
        this.tolerance = otolerance;
        this.maxRepeats = omaxRepeats;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void init(double ofx) {
        prevfx = ofx;
    }

    public boolean stop(double ofx) {
        boolean t = 2.0 * Math.abs(ofx - prevfx) <= tolerance * (Math.abs(ofx) + Math.abs(prevfx) + epsilon);
//        System.out.println("prevfx = " + prevfx);
//        System.out.println("ofx = " + ofx);

        if (t) {
            if (repeats == maxRepeats) {
                return true;
            } else {
                repeats++;
                prevfx = ofx;
                return false;
            }
        } else {
            repeats = 0;
            prevfx = ofx;
            return false;
        }
    }


}
