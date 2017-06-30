package game.evolution.treeEvolution;

/**
 * container for mutations
 */
public class TreeNodeWithDepth {
    public TreeNode node;
    public int depth;

    public TreeNodeWithDepth(TreeNode node, int depth) {
        this.node = node;
        this.depth = depth;
    }
}
