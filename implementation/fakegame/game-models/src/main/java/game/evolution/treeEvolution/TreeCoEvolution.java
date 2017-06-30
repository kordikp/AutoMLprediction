package game.evolution.treeEvolution;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Implementation TreeEvolution using coEvolution algorithm
 * Author: cernyjn
 */
public class TreeCoEvolution extends TreeEvolution {

    protected ArrayList<CoEvolution> coEvolution;
    protected int numIndividualsBetweenCoEv = 10;
    protected int numIndividuals = 0;

    public TreeCoEvolution(FitnessNode[] templates) {
        super(templates);
        coEvolution = new ArrayList<CoEvolution>();
    }

    protected void variableMutation(TreeNode root, int i) {
        NodeInformation currentTemplate = root.templateNode;
        if (currentTemplate.useCoEvolution.length != 0 && currentTemplate.useCoEvolution[i] >= 0) {
            Object input = coEvolution.get(currentTemplate.useCoEvolution[i]).selectIndividual();
            try {
                currentTemplate.setMethods[i].invoke(root.node, input);
            } catch (IllegalAccessException e) {
                log.warn("EXCEPTION: " + e.getMessage());
            } catch (InvocationTargetException e) {
                log.warn("EXCEPTION: " + e.getMessage());
            }
        } else {
            super.variableMutation(root, i);
        }
    }

    protected void applyEvolutionOperators() {
        if (coEvolution != null) {
            updateCoEvolution();
            numIndividuals += generation.length;
        }

        super.applyEvolutionOperators();
    }

    private void updateCoEvolution() {
        //send fitness update to coEvolution
        updateFitness();
        //run coEvolution if there was enough individuals evaluated
        if (numIndividuals > numIndividualsBetweenCoEv) {
            for (int i = 0; i < coEvolution.size(); i++) coEvolution.get(i).nextIteration();
            numIndividuals = numIndividuals - numIndividualsBetweenCoEv;
        }
    }

    /**
     * Method traverses every node of every individual and if it has variable that should be supplied by coEvolution it
     * sends given value and corresponding fitness of the individual into coEvolution
     */
    protected void updateFitness() {
        Stack<TreeNode> stack = new Stack<TreeNode>();

        TreeNode node;
        InnerTreeNode innerNode;
        int[] useCoEvolution;
        for (int i = 0; i < generation.length; i++) {
            stack.push(generation[i]);
            while (!stack.isEmpty()) {
                node = stack.pop();
                if (node instanceof InnerTreeNode) {
                    innerNode = (InnerTreeNode) node;
                    for (int j = 0; j < innerNode.getNodesNumber(); j++) {
                        stack.push(innerNode.getNode(j));
                    }
                }

                useCoEvolution = node.templateNode.useCoEvolution;
                try {
                    for (int j = 0; j < useCoEvolution.length; j++) {
                        if (useCoEvolution[j] >= 0) {
                            coEvolution.get(useCoEvolution[j]).saveFitness(node.templateNode.getMethods[j].invoke(node.node), fitness[i]);
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.warn("EXCEPTION: " + e.getMessage());
                } catch (InvocationTargetException e) {
                    log.warn("EXCEPTION: " + e.getMessage());
                }
            }
        }
    }


    /**
     * SET AND GET METHODS
     */
    public void setCoEvolution(CoEvolution coEvolution) {
        this.coEvolution.add(coEvolution);
    }

    public void setCoEvolution(ArrayList<CoEvolution> coEvolution) {
        this.coEvolution = coEvolution;
    }

    public ArrayList<CoEvolution> getCoEvolution() {
        return coEvolution;
    }

    public void setCoEvolutionTarget(CoEvolution coEvolution, Class cls, String variableName) {
        Stack<NodeInformation> stack = new Stack<NodeInformation>();
        InnerTreeNode innerNode;
        NodeInformation node;
        String varNameGetMethod = "get" + variableName.toLowerCase();
        for (int i = 0; i < objectsAvailable.length; i++) {
            stack.push(objectsAvailable[i]);
            while (!stack.isEmpty()) {
                node = stack.pop();
                if (node.template instanceof InnerTreeNode) {
                    innerNode = (InnerTreeNode) node.template;
                    for (int j = 0; j < innerNode.getNodesNumber(); j++) {
                        stack.push(innerNode.getNode(j).templateNode);
                    }
                }
                //do not check variables of different classes
                if (node.template.node.getClass() != cls) continue;

                for (int j = 0; j < node.getMethods.length; j++) {
                    //if methods name equals given variable name + get prefix
                    if (node.getMethods[j].getName().toLowerCase().equals(varNameGetMethod)) {
                        node.useCoEvolution = new int[node.getMethods.length];
                        for (int k = 0; k < node.useCoEvolution.length; k++) node.useCoEvolution[k] = -1;

                        node.useCoEvolution[j] = getCoEvolutionIndex(coEvolution);
                        break;
                    }
                }
            }
        }
    }

    private int getCoEvolutionIndex(CoEvolution coEv) {
        for (int i = 0; i < coEvolution.size(); i++) {
            if (coEvolution.get(i) == coEv) return i;
        }
        return -1;
    }

}
