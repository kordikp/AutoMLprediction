package game.utils;

/**
 * singleton with global random seed
 */
public class GlobalRandom extends MyRandom {
    private static GlobalRandom gr;

    private GlobalRandom() {
        super(100);
        // this.setSeed(-12345);
    }

    public static GlobalRandom getInstance() {
        if (gr == null) gr = new GlobalRandom();
        return gr;
    }
}
