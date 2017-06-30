package game.evolution.treeEvolution;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * Object to carry all support data for all available objects.
 */
public class NodeInformation implements Serializable {
    public TreeNode template;
    public Method[] getMethods;
    public Method[] setMethods;
    public double[] minVal;
    public double[] maxVal;
    //real tree depth
    public int depth;
    //represents value of the node towards counting of the tree depth
    public int depthWeight;
    //max depth where insert can be performed
    public int maxInsertDepth;
    public NodeInformation[] canMutateTo;
    public NodeInformation[] canMutateToLeaf;
    public NodeInformation[] addMutationLeaf;
    public boolean nodeGrowingMutation;
    public int[] useCoEvolution;

    public NodeInformation(TreeNode node) {
        fillEmptyReferences(node);
        template = node;
        depthWeight = 1;

        getMethods = new Method[0];
        setMethods = new Method[0];
        canMutateTo = null;
        canMutateToLeaf = null;
        addMutationLeaf = null;
        minVal = null;
        maxVal = null;

        nodeGrowingMutation = false;
        useCoEvolution = new int[0];
    }

    private void fillEmptyReferences(TreeNode node) {
        if (node.templateNode == null) node.templateNode = this;
        if (node instanceof InnerTreeNode) {
            InnerTreeNode innerNode = (InnerTreeNode) node;
            for (int i = 0; i < innerNode.getNodesNumber(); i++) {
                fillEmptyReferences(innerNode.getNode(i));
            }
        }
    }

}
