package game.clusters;

/**
 * Adopted from http://www.javaworld.com/javaworld/jw-08-2008/jw-08-java-wiki-extensions.html
 * Add Java extensions to your wiki
 * Java applets can bring dynamic functionality to your wiki pages
 * By Randall Scarberry, JavaWorld.com, 08/12/08
 */

/**
 * Class to represent a cluster of coordinates.
 */
public class Cluster {

    // Indices of the member coordinates.
    private int[] memberIndexes;
    // The cluster center.
    private double[] center;

    /**
     * Constructor.
     *
     * @param memberIndexes indices of the member coordinates.
     * @param center        the cluster center.
     */
    public Cluster(int[] memberIndexes, double[] center) {
        this.memberIndexes = memberIndexes;
        this.center = center;
    }

    /**
     * Get the member indices.
     *
     * @return an array containing the indices of the member coordinates.
     */
    public int[] getMemberIndexes() {
        return memberIndexes;
    }

    /**
     * Get the cluster center.
     *
     * @return a reference to the cluster center array.
     */
    public double[] getCenter() {
        return center;
    }

    /**
     * Get the size of the cluster.
     *
     * @return
     */
    public int getSize() {
        return memberIndexes != null ? memberIndexes.length : 0;
    }
}
