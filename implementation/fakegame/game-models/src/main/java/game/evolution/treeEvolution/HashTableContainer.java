package game.evolution.treeEvolution;

import java.io.Serializable;

/**
 * Container for context used in hash tables
 */
public class HashTableContainer implements Serializable, Cloneable {
    public TreeNode node;
    public double validFitness;
    public double testFitness;
    public int occurrences;

    public HashTableContainer clone() {
        HashTableContainer newObject;
        try {
            newObject = (HashTableContainer) super.clone();
            if (node != null) newObject.node = node.clone();
            return newObject;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
