package game.evolution.treeEvolution;

import java.io.Serializable;

/**
 * Node used in tree construction to hold additional information about node
 */
public class TreeNode implements Serializable, Cloneable {
    public FitnessNode node;
    public NodeInformation templateNode;

    public TreeNode clone() {
        FitnessNode innerTree = node.clone();
        TreeNode outerTree = cloneTree(innerTree);
        return outerTree;
    }

    public TreeNode cloneTree(FitnessNode innerTree) {
        TreeNode newTree = new TreeNode(innerTree, templateNode);
        return newTree;
    }

    public String toString() {
        return node.toString();
    }

    public TreeNode(FitnessNode node, NodeInformation sourceTemplate) {
        this.node = node;
        templateNode = sourceTemplate;
    }
}
