package common;

/**
 * User: honza
 * Date: 18.2.2007
 * Time: 17:38:14
 * MachineAccuracy - constants determining machine accuracy.
 * Similar to PAL class (http://www.pal-project.org)
 * TODO check this!
 */
public class MachineAccuracy {
    public static final double EPSILON; //2.220446049250313E-16
    public static final double SQRT_EPSILON; //1.4901161193847656E-8

    static {
        double eps = 0.5;
        while (1 + eps > 1) {
            eps /= 2.0;
        }
        eps *= 2.0;

        //EISPACK
//        double a = 4.0 / 3.0;
//        double b, c;
//        do {
//            b = a - 1.0;
//            c = b + b + b;
//            eps = Math.abs(c - 1.0);
//
//        } while (eps == 0.0);

        EPSILON = eps;
        SQRT_EPSILON = Math.sqrt(EPSILON);
    }
}
