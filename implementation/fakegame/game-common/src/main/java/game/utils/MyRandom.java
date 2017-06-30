/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.utils;

/**
 * implemented for generating random numbers without repeticion
 */
public class MyRandom extends java.util.Random implements java.io.Serializable {
    private boolean[] prev;
    private int myRand;
    private int vTest;
    private int maxVal;
    private int[] learn;
    private int[] test;

    public MyRandom(int maxValue) {
        super();
        maxVal = maxValue;
        prev = new boolean[maxVal];
        resetRandom();
    }

    public void resizeRandom(int maxValue) {
        maxVal = maxValue;
        prev = new boolean[maxValue];
        for (int i = 0; i < maxValue; i++)
            prev[i] = false;
    }

    public void resetRandom() {
        for (int i = 0; i < maxVal; i++)
            prev[i] = false;
    }

    public int getRandom(int num) {
        myRand = nextInt(num);
        if (prev[myRand]) {
            int frst = myRand;
            prev[frst] = false; // just one round if all generated
            do {
                myRand = (myRand + 1) % num; // find first not generated
            } while (prev[myRand]);
            prev[frst] = true; // set it back
        }
        prev[myRand] = true; // mark it generated
        return myRand;
    }

    public int nextInt(int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");
        if ((n & -n) == n) return (int) ((n * (long) next(31)) >> 31);
        int bits, val;
        do {
            bits = next(31);
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    public int getAtLeastRandom(int min, int max) {
        if (max <= min) return min;
        return (nextInt(max - min) + min);
    }

    public void generateLearningAndTestingSet(int vectorsInTestingSet) {
        resetRandom();
        vTest = vectorsInTestingSet;
        learn = new int[maxVal - vTest];
        test = new int[vTest];
        for (int i = 0; i < maxVal; i++) {
            if (i < vTest) test[i] = getRandom(maxVal);
            else learn[i - vTest] = getRandom(maxVal);
        }
    }

    public int getRandomLearningVector() {
        return learn[getRandom(maxVal - vTest)];
    }

    public int getRandomTestingVector() {
        return test[getRandom(vTest)];
    }

    public int getBootstrapRandomLearningVector() {
        return learn[nextInt(maxVal - vTest)];
    }

    public int getBootstrapRandomTestingVector() {
        return test[nextInt(vTest)];
    }

    public double getSmallDouble() {
        return (nextDouble() * 2.0 - 1.0) / 3.0;
    }

    public int[] getLearn() {
        return learn;
    }

    public int[] getTest() {
        return test;
    }
}
