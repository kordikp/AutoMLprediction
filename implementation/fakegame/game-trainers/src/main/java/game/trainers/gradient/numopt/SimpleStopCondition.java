package game.trainers.gradient.numopt;

/**
 * User: honza
 * Date: 21.2.2007
 * Time: 22:17:46
 * Inspired by Numerical Recipes.
 */
public class SimpleStopCondition extends StopCondition {
    private double epsilon = 1.0e-10;
    private double tolerance;
//    private ObjectiveFunction func; //TODO not needed now

    private double prevfx;


    public SimpleStopCondition(double otolerance) {
//        this.func = ofunc;
        this.tolerance = otolerance;
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
        System.out.println("prevfx = " + prevfx);
        System.out.println("ofx = " + ofx);
        prevfx = ofx;
        return t;
    }


}
