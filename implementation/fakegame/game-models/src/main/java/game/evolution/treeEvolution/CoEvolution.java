package game.evolution.treeEvolution;

/**
 * Interface for coEvolution algorithms.
 */
public interface CoEvolution {
    public Object selectIndividual();

    public void saveFitness(Object individual, double fitness);

    public void nextIteration();
}
