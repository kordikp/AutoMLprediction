package game.trainers.gartou;

import java.util.Random;

/**
 * <p>Title: Gartou Library - general methods and constants</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Anicka Kucerova, java version Jan Drchal
 * @version 1.0
 */

public final class General {
    private static Random seed;

    public static void initializeRandoms() {
        seed = new Random(1111);
    }

    public static int randomInt(int omin, int omax) {
        return seed.nextInt(omax - omin + 1) + omin;
    }

    public static double randomDouble(double omin, double omax) {
        return (omax - omin) * seed.nextDouble() + omin;
    }
}