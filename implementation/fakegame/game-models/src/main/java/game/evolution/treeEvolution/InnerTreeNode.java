package game.evolution.treeEvolution;

import java.util.Vector;

/**
 * Inner node used in tree construction to hold additional information about node.
 */
public class InnerTreeNode extends TreeNode {
    private Vector<TreeNode> successors;

    public TreeNode cloneTree(FitnessNode innerTree) {
        InnerFitnessNode innerFitnessNode = (InnerFitnessNode) innerTree;
        InnerTreeNode newTree = new InnerTreeNode(innerFitnessNode, templateNode);
        for (int i = 0; i < successors.size(); i++) {
            newTree.successors.add(successors.get(i).cloneTree(innerFitnessNode.getNode(i)));
        }
        return newTree;
    }

    public InnerTreeNode(InnerFitnessNode node, NodeInformation sourceTemplate) {
        super(node, sourceTemplate);
        successors = new Vector<TreeNode>();
    }

    public int getNodesNumber() {
        return successors.size();
    }

    public TreeNode getNode(int index) {
        return successors.get(index);
    }

    public void setNode(int index, TreeNode treeNode) {
        successors.set(index, treeNode);
        ((InnerFitnessNode) node).setNode(index, treeNode.node);
    }

    public void setNode(TreeNode oldNode, TreeNode newNode) {
        for (int i = 0; i < successors.size(); i++) {
            if (successors.get(i) == oldNode) {
                successors.set(i, newNode);
                break;
            }
        }
        InnerFitnessNode innerFitnessNode = (InnerFitnessNode) node;
        for (int i = 0; i < innerFitnessNode.getNodesNumber(); i++) {
            if (innerFitnessNode.getNode(i) == oldNode.node) {
                innerFitnessNode.setNode(i, newNode.node);
                break;
            }
        }
    }

    public void removeNode(int index) {
        successors.remove(index);
        ((InnerFitnessNode) node).removeNode(index);
    }

    public void removeNode(TreeNode treeNode) {
        successors.remove(treeNode);
        ((InnerFitnessNode) node).removeNode(treeNode.node);
    }

    public void addNode(TreeNode treeNode) {
        successors.add(treeNode);
        ((InnerFitnessNode) node).addNode(treeNode.node);
    }

    public void addNodeRef(TreeNode treeNode) {
        successors.add(treeNode);
    }

}
