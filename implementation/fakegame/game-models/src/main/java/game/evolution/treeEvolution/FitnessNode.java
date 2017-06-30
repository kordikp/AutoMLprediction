package game.evolution.treeEvolution;

/**
 * Interface for objects used by evolution algorithms which need fitness calculations.
 * Author: cernyjn
 */
public interface FitnessNode extends Cloneable {

    public FitnessNode clone();

    public String toString();

}
