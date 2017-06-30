/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Ant Colony Optimization (ACO*) - Ant</p>
 * <p>Description: class for ACO ant</p>
 */

package game.trainers.ant.aco;

class Ant implements Comparable {
    public int dimensions;        // vector of variables size
    public double pError;        // present error
    public double pVector[];    // present vector
    public double gWeight;        // Gauss curves weight

    /**
     * constructor for ant
     *
     * @param dimensions number of parameters to optimize
     */
    public Ant(int dimensions) {
        this.dimensions = dimensions;
        pVector = new double[dimensions];

        // random solution init (-10, 10)
        for (int i = 0; i < dimensions; i++) {
            pVector[i] = (Math.random() * 20.0) - 10.0;
        }
        pError = Double.POSITIVE_INFINITY;
    }

    /**
     * compareTo function for sorting ants according to their pError
     *
     * @param a object to compare with
     * @return result of comparison
     */
    public int compareTo(Object a) {
        Ant ant = (Ant) a;
        if (this.pError < ant.pError) return -1;
        if (this.pError > ant.pError) return 1;
        return 0;
    }
}
