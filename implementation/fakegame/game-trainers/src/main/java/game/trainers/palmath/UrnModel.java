// Urn.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package game.trainers.palmath;


/**
 * class for drawing numbers from an urn with and
 * without laying back
 *
 * @author Korbinian Strimmer
 * @version $Id: UrnModel.java,v 1.2 2007/05/01 15:25:31 kordikp Exp $
 */
public class UrnModel implements java.io.Serializable {
    //
    // Public stuff
    //

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * create urn model
     *
     * @param n capacity (corresponding to numbers 0..n-1)
     */
    public UrnModel(int n) {
        capacity = n;
        isAvailable = new boolean[n];
        rng = new MersenneTwisterFast();
        reset();
    }

    /**
     * draw a number without putting back
     *
     * @return number drawn (random integer between 0..n-1, if empty return -1)
     */
    public int drawDontPutBack() {
        if (numNumbers == numDrawn) {
            next = -1;
        } else {
            draw();
            isAvailable[next] = false;
            numDrawn++;
        }
        return next;
    }

    /**
     * refill urn
     */
    void reset() {
        numNumbers = capacity;
        numDrawn = 0;
        for (int i = 0; i < capacity; i++) {
            isAvailable[i] = true;
        }
    }

    /**
     * draws a number with putting back
     *
     * @return number drawn (random integer between 0..n-1, if empty return -1)
     */
    public int drawPutBack() {
        if (numNumbers == numDrawn) {
            next = -1;
        } else {
            draw();
        }
        return next;
    }

    //
    // Private stuff
    //

    private int capacity, numNumbers, numDrawn, next;
    private boolean[] isAvailable;
    private MersenneTwisterFast rng;

    private void draw() {
        // Random integer 0..numNumbers-numDrawn-1
        int i = rng.nextInt(numNumbers - numDrawn);
        next = -1;
        int k = -1;
        do {
            next++;
            if (isAvailable[next]) {
                k++;
            }
        }
        while (k != i);
    }
}
