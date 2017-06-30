package game.trainers.gradient.numopt;

/**
 * User: drchaj1
 * Date: 22.3.2007
 * Time: 1:21:54
 * TODO see Bulirsh and Stoer (ref in Nocedal page 57)
 */
public class CubicInterpolation {
    public static double interpolate(double oa, double ob, double ofa, double ofb, double oslopeA, double oslopeB, double ox) {
        double p; // cubic coefficient for x^3
        double q; // cubic coefficient for x^2
        double delta = ob - oa;
        double delta2 = delta * delta;
        double delta3 = delta2 * delta;
        double xa = ox - oa;
        double xa2 = xa * xa;
        double xa3 = xa2 * xa;
        p = (2 * (ofa - ofb) + delta * (oslopeA + oslopeB)) / delta3;
        q = (3 * (ofb - ofa) - delta * (2 * oslopeA + oslopeB)) / delta2;
        return p * xa3 + q * xa2 + oslopeA * xa + ofa;
    }

    /**
     * Finds minimum of a cubic polynomial given by c(a), c'(a), c(b), c'(b).
     * See Jorge Nocedal, Stephen J. Wright: Numerical Optimization,
     * Springer-Verlag, 1999, page 57
     *
     * @param oa      a
     * @param ob      b
     * @param ofa     c(a)
     * @param ofb     c(b)
     * @param oslopeA c'(a)
     * @param oslopeB c'(b)
     * @return
     */
    public static double interpolateAndMinimize(double oa, double ob, double ofa, double ofb, double oslopeA, double oslopeB) {
//        System.out.println("IM oa = " + oa + " ob = " + ob + " ofa = " + ofa + " ofb = " + ofb + " oslopeA = " + oslopeA + " oslopeB = " + oslopeB);
        double d1;
        double d2;
        double delta = ob - oa;
        d1 = oslopeA + oslopeB - 3 * ((ofa - ofb) / delta);
//        System.out.println("d1 * d1 - oslopeA * oslopeB = " + (d1 * d1 - oslopeA * oslopeB));
        d2 = Math.sqrt(d1 * d1 - oslopeA * oslopeB);
        return ob - delta * ((oslopeB + d2 - d1) / (oslopeB - oslopeA + 2 * d2));
    }

    public static void main(String[] args) {
        System.out.println("x: " + interpolateAndMinimize(0.0, 1.0, 0.0, 0.0, -1.0, 1.0));
        System.out.println("f: " + interpolate(0.0, 2.0, 0.0, 0.0, -1.0, 1.0, 1.0));
    }
}
