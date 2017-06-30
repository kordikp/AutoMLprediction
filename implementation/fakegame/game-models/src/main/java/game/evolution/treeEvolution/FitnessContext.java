package game.evolution.treeEvolution;

/**
 * Helper interface to determine objects fitness.
 */
public interface FitnessContext {

    public double getFitness(TreeNode obj);

    public double[] getFitness(Object[] obj);

    public double verifyBestNode();

    public double getBestFitness();

    public TreeNode getBestNode();

    public double verifyFitness(TreeNode obj);

}
