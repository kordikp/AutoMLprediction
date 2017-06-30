package game.evolution.treeEvolution;

/**
 * Interface for objects used by evolution algorithms which need fitness calculations.
 * Author: cernyjn
 */
public interface InnerFitnessNode extends FitnessNode {

    public FitnessNode getNode(int index);

    public void setNode(int index, FitnessNode node);

    public void addNode(FitnessNode node);

    public int getNodesNumber();

    public void removeNode(int index);

    public void removeNode(FitnessNode node);
}
