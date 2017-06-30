package game.evolution.treeEvolution;

import game.utils.MyRandom;
import org.apache.log4j.Logger;
import org.ytoh.configurations.ui.SelectionSetModel;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static java.lang.Math.max;
import static java.lang.Math.random;

/**
 * Global tree evolution
 * Author: cernyjn
 * maximize fitness
 */
public class TreeEvolution {

    protected FitnessContext fit;

    protected TreeNode[] generation;
    protected int[] age;
    protected double[] fitness;
    protected NodeInformation[] leafNodes;
    protected NodeInformation[] objectsAvailable = new NodeInformation[0];
    protected FitnessNode[] initGeneration;
    protected Logger log;

    protected int maxTreeDepth = 5;
    protected int generationSize = 10;
    protected int maxGenerations = 100;
    //disabled as default
    protected int stopIfNoChangeForNGen = Integer.MAX_VALUE;
    protected double nodeChangeMutationProb = 0.5;
    protected double nodeAddMutationProb = 0.1;
    protected double variableMutationProb = 0.7;
    protected int showFitness = -1;
    protected double localMutationThreshold = 0.8;

    protected int currentGeneration;
    protected double minFitness;
    protected double avgFitness;
    protected int individualCount;
    protected double defaultVarDispersion = 3;
    protected int noChangeForNGen;
    protected Random rnd;

    protected class TreeNodeWithChanges {
        public TreeNode node;
        public int changes;

        public TreeNodeWithChanges(TreeNode node, int changes) {
            this.node = node;
            this.changes = changes;
        }
    }

    public TreeEvolution(FitnessNode[] templates) {
        objectsAvailable = new NodeInformation[templates.length];
        for (int i = 0; i < objectsAvailable.length; i++) {
            objectsAvailable[i] = new NodeInformation(createTemplate(templates[i]));
        }
        log = Logger.getLogger(this.getClass());
    }

    public void init(FitnessNode[] initGeneration, FitnessContext fit) {
        this.fit = fit;
        this.initGeneration = initGeneration;

        rnd = new Random(System.nanoTime());

        currentGeneration = 0;
        noChangeForNGen = 0;
        minFitness = Double.MAX_VALUE;
        //really small value, but not entirely maximum negative value, to allow some operation without creating negative infinity
        avgFitness = -1 * Double.MAX_VALUE / 100;
        individualCount = 0;

        computeLeafNodes();
    }

    /**
     * Creates initial generation from previously supplied FitnessNodes
     */
    protected void initGeneration() {
        MyRandom rndWithoutRep = new MyRandom(initGeneration.length);

        generation = new TreeNode[generationSize];
        age = new int[generationSize];
        fitness = new double[generationSize];
        int idx;
        for (int i = 0; i < generationSize; i++) {
            idx = rndWithoutRep.getRandom(initGeneration.length);
            generation[i] = createTemplate(initGeneration[idx]);
        }
    }

    /**
     * Recursive encapsulation of the FitnessNode tree with TreeNode tree. Non-recursive starting function is needed, because clone need to
     * be run only 1 times, because it clones whole tree.
     *
     * @param node FitnessNode tree.
     * @return Returns tree with all nodes encapsulated with TreeNodes.
     */
    public TreeNode createTemplate(FitnessNode node) {
        return encapsulateWithTreeNode(node.clone());
    }

    protected TreeNode encapsulateWithTreeNode(FitnessNode node) {
        TreeNode nodeTemplate;
        NodeInformation template = findTemplate(node);

        if (node instanceof InnerFitnessNode) {
            InnerFitnessNode innerF = (InnerFitnessNode) node;
            nodeTemplate = new InnerTreeNode(innerF, template);
        } else {
            nodeTemplate = new TreeNode(node, template);
        }

        if (node instanceof InnerFitnessNode) {
            InnerFitnessNode innerNode = (InnerFitnessNode) node;
            InnerTreeNode innerTemplate = (InnerTreeNode) nodeTemplate;
            for (int i = 0; i < innerNode.getNodesNumber(); i++) {
                //add just references to the outer node, inner node references already exists due to FitnessNode clone method
                innerTemplate.addNodeRef(encapsulateWithTreeNode(innerNode.getNode(i)));
            }
        }
        return nodeTemplate;
    }

    /**
     * @param node Node to which we want to find its template.
     * @return Returns template of the input parameter node. If template does not exists return null.
     */
    protected NodeInformation findTemplate(FitnessNode node) {
        for (int i = 0; i < objectsAvailable.length; i++) {
            if (objectsAvailable[i] == null) break;
            if (objectsAvailable[i].template.node.getClass().equals(node.getClass())) {
                return objectsAvailable[i];
            }
        }
        //template not found - if last item of objectsAvailable is null, templates are generated so return null(is replaced by self reference)
        if (objectsAvailable[objectsAvailable.length - 1] == null) {
            return null;
        } else { //otherwise add node into templates
            log.info("adding into templates: " + node.toString());
            NodeInformation[] newTemplates = new NodeInformation[objectsAvailable.length + 1];
            System.arraycopy(objectsAvailable, 0, newTemplates, 0, objectsAvailable.length);
            objectsAvailable = newTemplates;

            objectsAvailable[objectsAvailable.length - 1] = new NodeInformation(createTemplate(node));
            return objectsAvailable[objectsAvailable.length - 1];
        }
    }

    /**
     * Runs genetic algorithm, stops if maximum number of generations is reached (set via setMaxGenerations) or if
     * there is no change in best solution for N generations (set via stopIfNoChangeForNGen).
     */
    public void run() {
        if (generation == null) {
            computeTemplateDepths();
            initGeneration();
            printEvolutionSettings();
            if (fit.getBestNode() != null) {
                log.info("CONTEXT BEST FITNESS: " + fit.getBestFitness() * showFitness + " model:" + fit.getBestNode().toString());
            }
        }

        for (int i = currentGeneration; i < maxGenerations; i++) {
            if (checkStopConditions()) break;

            log.info("generation: " + i);
            survivalCheck();
            applyEvolutionOperators();
            noChangeForNGen++;
            currentGeneration++;
        }
    }

    /**
     * Computes all depths for all templates. Depths are as follows:
     * depth - real weighted depth of the tree (each node can have different weight of the depth)
     * insert depth - deepest point in the tree when insert can be performed (ie. where is node with 0 successors)
     */
    protected void computeTemplateDepths() {
        Stack<TreeNodeWithDepth> stack = new Stack<TreeNodeWithDepth>();
        TreeNodeWithDepth currentNode;
        InnerTreeNode currentInnerNode;
        int currentDepth;
        int maxDepth;
        int maxInsertDepth;
        for (int i = 0; i < objectsAvailable.length; i++) {
            maxInsertDepth = Integer.MIN_VALUE;
            maxDepth = 0;
            stack.push(new TreeNodeWithDepth(objectsAvailable[i].template, 0));
            while (!stack.isEmpty()) {
                currentNode = stack.pop();
                currentDepth = currentNode.depth + currentNode.node.templateNode.depthWeight;
                if (currentDepth > maxDepth) maxDepth = currentDepth;

                if (currentNode.node instanceof InnerTreeNode) {
                    currentInnerNode = (InnerTreeNode) currentNode.node;
                    if (currentInnerNode.getNodesNumber() == 0 && currentDepth > maxInsertDepth)
                        maxInsertDepth = currentDepth;

                    for (int j = 0; j < currentInnerNode.getNodesNumber(); j++) {
                        stack.push(new TreeNodeWithDepth(currentInnerNode.getNode(j), currentDepth));
                    }
                }
            }
            objectsAvailable[i].depth = maxDepth;
            objectsAvailable[i].maxInsertDepth = maxInsertDepth;
        }
    }

    protected void applyEvolutionOperators() {
        double relativeFitness;
        double bestFitness = fit.getBestFitness();
        for (int j = 0; j < generation.length; j++) {
            relativeFitness = (fitness[j] - avgFitness) / (bestFitness - avgFitness);
           // System.out.println("apply evo op for "+j+ ",  relfit("+relativeFitness+"), bestFit("+bestFitness+
           //         "), avgFit("+avgFitness+"), fitness["+j+"]("+fitness[j]+")");
            if (relativeFitness > localMutationThreshold) {
               // System.out.println("apply evo op for a");
                int numChanges = max(1,(int) ((1 - relativeFitness) * 10) + 1);
                generation[j] = restrictedMutation(generation[j], numChanges);
            } else {
               // System.out.println("apply evo op for b");
                generation[j] = mutateRe(generation[j], 0).node;
            }
            age[j]++;
        }
    }

    /**
     * @return Cheks if stop conditions apply, if so return true.
     */
    protected boolean checkStopConditions() {
        if (noChangeForNGen > stopIfNoChangeForNGen) {
            noChangeForNGen = 0;
            return true;
        }
        return false;
    }

    /**
     * Performs additional computation of fitness of best node to verify result stability.
     */
    public void verifyBestSolution() {
        //perform verifying until bestNode does not changes after that
        String bestNode;
        log.info("verifying best nodes");
        do {
            bestNode = fit.getBestNode().toString();
            fit.verifyBestNode();
            log.info("BEST FITNESS & NODE UPDATE: " + fit.getBestFitness() * showFitness + " model:" + fit.getBestNode().toString());
        } while (!fit.getBestNode().toString().equals(bestNode));
    }

    /**
     * Checks all individuals in generation and if individual passed his age (determined by fitness) then it is
     * removed and replaced by copy of so far best individual.
     */
    protected void survivalCheck() {
        double oldBestFitness = fit.getBestFitness();
        TreeNode oldBestNode = fit.getBestNode();

        fitness = fit.getFitness(generation);

        double bestFitness = fit.getBestFitness();
        TreeNode bestNode = fit.getBestNode();
        for (int i = 0; i < generation.length; i++) {

            //replace defective individual with best node copy
            if (fitness[i] == Double.NEGATIVE_INFINITY || Double.isNaN(fitness[i])) {
                replaceIndividualWithBestNode(i);
                continue;
            }
            //outlier elimination
            if (fitness[i] > avgFitness - 5 * Math.abs(avgFitness)) {
                if (fitness[i] < minFitness) {
                    minFitness = fitness[i];
                }
                //update average fitness
                avgFitness = (avgFitness * individualCount + fitness[i]) / (individualCount + 1);
                individualCount++;
            }
        }

        double relativeFitness;
        for (int i = 0; i < generation.length; i++) {
            relativeFitness = (fitness[i] - minFitness) / (bestFitness - minFitness);
            if (age[i] > max(Math.ceil(relativeFitness * Math.pow(max(maxGenerations, 20), 0.8)), maxTreeDepth + 1)) {
                //object dies and is replaced by copy of the best object
                log.debug("individual " + i + " dies at age " + age[i]);
                replaceIndividualWithBestNode(i);
            }
        }
        //best solution update
        if (oldBestNode == null || !oldBestNode.equals(bestNode)) {
            if (bestFitness > oldBestFitness) {
                log.info("NEW BEST FITNESS: " + bestFitness * showFitness + " model:" + bestNode.toString());
                adjustVariableMax(bestNode);
                noChangeForNGen = 0;
            } else {
                log.info("BEST FITNESS & NODE UPDATE: " + bestFitness * showFitness + " model:" + bestNode.toString());
                noChangeForNGen = noChangeForNGen / 2;
            }
        } else {
            if (oldBestFitness - bestFitness > 0.00000001) {
                log.info("BEST FITNESS UPDATE: " + bestFitness * showFitness + " model:" + bestNode.toString());
            }
        }

        //set fitness of individuals that is higher than current maximum to current maximum (this can happen if there are
        // 2 exactly same individuals and fitness estimate changes)
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > bestFitness) fitness[i] = bestFitness;
        }
       // System.out.println("::: end of survival check");
    }

    protected void replaceIndividualWithBestNode(int indexOfIndividual) {
        generation[indexOfIndividual] = fit.getBestNode().clone();
        age[indexOfIndividual] = 0;
        fitness[indexOfIndividual] = fit.getBestFitness();
    }

    /**
     * If some variable of the node is greater than % of its maximum value, maximum is resized.
     *
     * @param node Node to adjust its maximum values.
     */
    protected void adjustVariableMax(TreeNode node) {
        NodeInformation template = node.templateNode;
        for (int i = 0; i < template.getMethods.length; i++) {
            try {
                Object value = template.getMethods[i].invoke(node.node);

                double dValue;
                if (value instanceof Integer) dValue = ((Integer) value).doubleValue();
                else if (value instanceof Double) dValue = (Double) value;
                else continue;

                if (dValue > template.maxVal[i] / 2) {
                    String methodName = node.node.getClass().getSimpleName() + "." + template.getMethods[i].getName().substring(3);
                    log.info("RESIZING MAXVAL OF " + methodName + " " + template.maxVal[i] + "->" + template.maxVal[i] * 2);
                    template.maxVal[i] = template.maxVal[i] * 2;
                }
            } catch (IllegalAccessException e) {
                log.warn("EXCEPTION: " + e.getMessage());
            } catch (InvocationTargetException e) {
                log.warn("EXCEPTION: " + e.getMessage());
            }
        }

        if (node instanceof InnerTreeNode) {
            InnerTreeNode innerNode = (InnerTreeNode) node;
            for (int i = 0; i < innerNode.getNodesNumber(); i++) {
                adjustVariableMax(innerNode.getNode(i));
            }
        }
    }

    /**
     * Iterative implementation of mutation that is restricted by number of changes it can do. It is slower than recursive version.
     * Function limits number of mutations to the number of nodes in the tree. Also generates random number of mutations limited
     * byt given maximum, see getRandomMaxChanges function for that.
     *
     * @param root       Root node of the tree.
     * @param maxChanges Maximum number of allowed changes (function can perform less changes).
     * @return Returns mutated tree.
     */
    protected TreeNode restrictedMutation(TreeNode root, int maxChanges) {
      //  System.out.println("restrictedMutation ("+maxChanges+")");
        ArrayList<TreeNodeWithDepth[]> nodes = linearizeTree(root);
      //  System.out.println("restrictedMutation1");
        maxChanges = Math.min(nodes.size(), maxChanges);
      //  System.out.println("restrictedMutation2 " + maxChanges);
        maxChanges = getRandomMaxChanges(maxChanges);
      //  System.out.println("restricted mutation maxchanges " + maxChanges);
        double first2probSum = nodeChangeMutationProb + nodeAddMutationProb;
        double probSum = first2probSum + variableMutationProb;
        double rndNum;
        while (maxChanges > 0) {
            TreeNodeWithDepth[] selectedNodes = nodes.get(rnd.nextInt(nodes.size()));
            rndNum = rnd.nextDouble() * probSum;

            if (rndNum < nodeChangeMutationProb) { //NODE CHANGE
                System.out.println("node change "+ maxChanges);
                TreeNode newNode = nodeChangeMutation(selectedNodes[1].node, selectedNodes[1].depth);
                TreeNodeWithChanges changed = getNumberOfChangedNodes(selectedNodes[1].node, newNode, maxChanges);

                if (changed.changes > 0) {
                    //fix parent links
                    if (selectedNodes[0].node == null) root = changed.node;
                    else ((InnerTreeNode) selectedNodes[0].node).setNode(selectedNodes[1].node, changed.node);

                    maxChanges -= changed.changes;
                }
                if (maxChanges > 0) nodes = linearizeTree(root);
            } else if (rndNum < first2probSum) { //NODE ADD
                System.out.println("node add " +maxChanges);
                maxChanges -= addNodeMutation(selectedNodes[1].node, selectedNodes[1].depth);
                if (maxChanges > 0) nodes = linearizeTree(root);
            } else { //VARIABLE MUTATION
               // System.out.println("variable mutation "+maxChanges);
                maxChanges -= max(1,mutateVariables(selectedNodes[1].node, maxChanges));
            }
        }
     //   System.out.println("restricted mutation after while");
        return root;
    }

    /**
     * @param maxChanges Maximum number of allowed changes.
     * @return Returns maximum number of changes defined by gaussian distribution where the peak of the gaussian is at the
     * maxChanges number. All greater numbers are rounded to this number. All number smaller than 0 are generated again.
     */
    protected int getRandomMaxChanges(int maxChanges) {
        int newMaxChanges;
        do {
            newMaxChanges = (int) Math.round(rnd.nextGaussian() * maxChanges + maxChanges);
            if (newMaxChanges > maxChanges) newMaxChanges = maxChanges;
        } while (newMaxChanges <= 0);
        return newMaxChanges;
    }

    /**
     * @param oldNode    Node that was given into the nodeChange mutation.
     * @param newNode    Node that was returned by nodeChange mutation (possibly modified).
     * @param maxChanges Maximum number of allowed changes left.
     * @return Returns root node of the tree and number of changes made by previous nodeChange mutation. If mutation node -> leaf
     * was performed (= delete mutation), then removeNode function is ran onto the old tree to remove specified number of nodes(=maxChanges)
     * from that tree.
     */
    protected TreeNodeWithChanges getNumberOfChangedNodes(TreeNode oldNode, TreeNode newNode, int maxChanges) {
        if (oldNode == newNode) return new TreeNodeWithChanges(oldNode, 0);

        if (oldNode instanceof InnerTreeNode && (isEncapsulatedLeaf(newNode) || !(newNode instanceof InnerTreeNode))) {
            int changes = 0;
            TreeNode modifiedNode = oldNode;
            for (int i = 0; i < maxChanges; i++) {
                TreeNodeWithChanges cleared = removeNode(modifiedNode);
                if (cleared.changes > 0) {
                    changes += cleared.changes;
                    modifiedNode = cleared.node;
                } else {
                    break;
                }
            }
            return new TreeNodeWithChanges(modifiedNode, changes);
        } else {
            return new TreeNodeWithChanges(newNode, 1);
        }
    }

    /**
     * Method tries to mutate all variables of given node, maximum to number of maxChanges. It does not guarantee any change.
     *
     * @param root       Root node of the tree.
     * @param maxChanges Maximum number of variables that can be changed.
     * @return Return number of changed variables.
     */
    protected int mutateVariables(TreeNode root, int maxChanges) {
        int numVariables = root.templateNode.getMethods.length;
        MyRandom rnd = new MyRandom(numVariables);
        int changes = 0;
        String prevNode;
        for (int i = 0; i < numVariables; i++) {
            if (changes >= maxChanges) break;
            prevNode = root.toString();
            variableMutation(root, rnd.getRandom(numVariables));
            if (!prevNode.equals(root.toString())) changes++;
        }
        return changes;
    }

    /**
     * @param root Root node of the tree.
     * @return Returns linearized list of pairs {parent_node_with_depth,current_node_with_depth}.
     */
    protected ArrayList<TreeNodeWithDepth[]> linearizeTree(TreeNode root) {
        ArrayList<TreeNodeWithDepth[]> nodes = new ArrayList<TreeNodeWithDepth[]>();
        Stack<TreeNodeWithDepth> stack = new Stack<TreeNodeWithDepth>();

        TreeNodeWithDepth[] newNode = new TreeNodeWithDepth[]{new TreeNodeWithDepth(null, -1), new TreeNodeWithDepth(root, 0)};
        nodes.add(newNode);
        stack.push(newNode[1]);

        TreeNodeWithDepth currentNode;
        InnerTreeNode currentInnerNode;
        while (!stack.isEmpty()) {
            currentNode = stack.pop();
            if (currentNode.node instanceof InnerTreeNode) {
                currentInnerNode = (InnerTreeNode) currentNode.node;
                for (int i = 0; i < currentInnerNode.getNodesNumber(); i++) {
                    newNode = new TreeNodeWithDepth[]{currentNode, new TreeNodeWithDepth(currentInnerNode.getNode(i), currentNode.depth + currentInnerNode.templateNode.depthWeight)};
                    nodes.add(newNode);
                    stack.push(newNode[1]);
                }
            }
        }
        return nodes;
    }

    /**
     * Function deletes 1 node from the given tree. It will delete either:
     * 1)leaf or encapsulated leaf which have one or more siblings
     * 2)inner node which has only 1 successor.
     *
     * @param root Root of the tree from which will be deleted.
     * @return Returns modified root with number of changes made.
     */
    protected TreeNodeWithChanges removeNode(TreeNode root) {
        if (!canBeDeleted(root)) return new TreeNodeWithChanges(root, 0);

        InnerTreeNode parent = null;
        TreeNode currentNode = root;
        int changes = 0;
        while (currentNode != null) {
            //if its leaf (ie leaf with some siblings) remove it
            if (!(currentNode instanceof InnerTreeNode) || isEncapsulatedLeaf(currentNode)) {
                parent.removeNode(currentNode);
                changes++;
                break;
                //if its inner node with 1 successor remove the inner node and put the leaf instead of him
            } else if (((InnerTreeNode) currentNode).getNodesNumber() == 1 && !currentNode.templateNode.nodeGrowingMutation) {
                if (parent == null) root = ((InnerTreeNode) currentNode).getNode(0);
                else parent.setNode(currentNode, ((InnerTreeNode) currentNode).getNode(0));

                changes++;
                break;
            }
            parent = (InnerTreeNode) currentNode;
            currentNode = getNextNode(currentNode);
        }
        return new TreeNodeWithChanges(root, changes);
    }

    /**
     * @param root Input node.
     * @return checks if the tree is trivial and thus cannot be deleted
     */
    protected boolean canBeDeleted(TreeNode root) {
        //is leaf
        if (!(root instanceof InnerTreeNode)) return false;
            //is encapsulated leaf node with single leaf successor
        else if (isEncapsulatedLeaf(root)) return false;
            //cannot be mutated
        else if (root.templateNode.canMutateTo != null && root.templateNode.canMutateTo.length == 0) return false;
        else return true;
    }

    /**
     * Node is encapsulated leaf if it has nodeGrowing mutation enabled and has 1 leaf successor
     *
     * @param node Input node.
     * @return Returns true if node is a Encapsulated leaf.
     */
    protected boolean isEncapsulatedLeaf(TreeNode node) {
        if (node.templateNode.nodeGrowingMutation && node instanceof InnerTreeNode && ((InnerTreeNode) node).getNodesNumber() == 1 &&
                !(((InnerTreeNode) node).getNode(0) instanceof InnerTreeNode)) return true;
        else return false;
    }

    /**
     * @param root Input tree.
     * @return Returns random successor or null if root is leaf.
     */
    protected TreeNode getNextNode(TreeNode root) {
        if (root instanceof InnerTreeNode)
            return ((InnerTreeNode) root).getNode(rnd.nextInt(((InnerTreeNode) root).getNodesNumber()));
        else return null;
    }

    /**
     * Recursive implementation of tree mutation.
     *
     * @param root     Root node, which will be mutated.
     * @param curDepth Current depth in the tree.
     * @return Returns root node of the new mutated tree.
     */
    protected TreeNodeWithDepth mutateRe(TreeNode root, int curDepth) {
        //NODE MUTATION
        if (rnd.nextDouble() < nodeChangeMutationProb) {
            root = nodeChangeMutation(root, curDepth);
        }

        //PERFORM MUTATION RECURSIVELY
        if (root instanceof InnerTreeNode) {
            InnerTreeNode currentNode = (InnerTreeNode) root;
            TreeNodeWithDepth tmpNode;
            int maxDepth = 0;
            for (int i = 0; i < currentNode.getNodesNumber(); i++) {
                tmpNode = mutateRe(currentNode.getNode(i), curDepth + currentNode.templateNode.depthWeight);
                if (tmpNode.depth > maxDepth) maxDepth = tmpNode.depth;
                currentNode.setNode(i, tmpNode.node);
            }
            curDepth = maxDepth;
        }

        //ADDING NODES TO INNER NODES
        if (root instanceof InnerTreeNode && rnd.nextDouble() < nodeAddMutationProb) {
            addNodeMutation(root, curDepth);
        }

        //MUTATE ROOT VARIABLES
        for (int i = 0; i < root.templateNode.getMethods.length; i++) {
            if (rnd.nextDouble() < variableMutationProb) variableMutation(root, i);
        }

        return new TreeNodeWithDepth(root, curDepth);
    }

    /**
     * Mutation of changing node from one type to another.
     *
     * @param root     Node to mutate.
     * @param curDepth Depth in the tree.
     * @return Returns changed root node.
     */
    protected TreeNode nodeChangeMutation(TreeNode root, int curDepth) {
        //check for mutation restrictions and use local mutation settings instead of global if needed
        NodeInformation[] objectsAvailable;
        if (root.templateNode.canMutateTo != null) objectsAvailable = root.templateNode.canMutateTo;
        else objectsAvailable = this.objectsAvailable;

        if (objectsAvailable.length == 0) return root;

        int newNodeIndex = rnd.nextInt(objectsAvailable.length);
        //create new tree node
        TreeNode newNode = objectsAvailable[newNodeIndex].template.clone();

        if (newNode instanceof InnerTreeNode && root instanceof InnerTreeNode && !root.templateNode.nodeGrowingMutation) { //NODE->NODE
            //maximum of depth of the tree or insert depth + previous subtree - root
            curDepth = curDepth + max(objectsAvailable[newNodeIndex].depth, objectsAvailable[newNodeIndex].maxInsertDepth + getTreeDepth(root) - root.templateNode.depthWeight);
            if (curDepth > maxTreeDepth) return root;

            InnerTreeNode currentNode = (InnerTreeNode) root;
            InnerTreeNode newInnerNode = (InnerTreeNode) newNode;
            if (newInnerNode.getNodesNumber() == 0) {
                for (int i = 0; i < currentNode.getNodesNumber(); i++) {
                    newInnerNode.addNode(currentNode.getNode(i));
                }
            } else { //if the newNode is more complex perform complex insert
                TreeNode[] nodesToInsert = new TreeNode[currentNode.getNodesNumber()];
                for (int i = 0; i < currentNode.getNodesNumber(); i++) {
                    nodesToInsert[i] = currentNode.getNode(i);
                }
                addToNewNode(newNode, nodesToInsert);
            }
        } else if (newNode instanceof InnerTreeNode) { //LEAF + GROW MUTATION -> NODE
            //maximum of depth of the tree or insert depth + previous subtree
            curDepth = curDepth + max(objectsAvailable[newNodeIndex].depth, objectsAvailable[newNodeIndex].maxInsertDepth + getTreeDepth(root));
            if (curDepth > maxTreeDepth) return root;

            InnerTreeNode newInnerNode = (InnerTreeNode) newNode;
            if (newInnerNode.getNodesNumber() == 0) {
                newInnerNode.addNode(root);
            } else { //if the newNode is more complex perform complex insert
                addToNewNode(newNode, new TreeNode[]{root});
            }
        } else { //LEAF->LEAF  + NODE->LEAF
            curDepth = curDepth + objectsAvailable[newNodeIndex].depth;
            if (curDepth > maxTreeDepth) return root;
            //do nothing, just return the leaf node
        }
        return newNode;
    }

    /**
     * @param newNode       Template which will be used.
     * @param nodesToInsert Nodes which will be inserted into template.
     * @return Returns template with inserted nodes at locations where are no leaf nodes in the template.
     */
    protected TreeNode addToNewNode(TreeNode newNode, TreeNode[] nodesToInsert) {
        Queue<TreeNode> nodes = new ArrayBlockingQueue<TreeNode>(20);
        nodes.add(newNode);
        TreeNode templateLeaf;
        InnerTreeNode templateNode;
        while (!nodes.isEmpty()) {
            templateLeaf = nodes.poll();
            if (templateLeaf instanceof InnerTreeNode) {
                templateNode = (InnerTreeNode) templateLeaf;
                //put all successors into queue
                for (int i = 0; i < templateNode.getNodesNumber(); i++) {
                    nodes.add(templateNode.getNode(i));
                }

                //add nodes to insert to current Node if it hasnt any successors
                if (templateNode.getNodesNumber() == 0) {
                    for (int i = 0; i < nodesToInsert.length; i++) {
                        templateNode.addNode(nodesToInsert[i].clone());
                    }
                }
            }
        }
        return newNode;
    }

    /**
     * Mutation of adding new child to a inner node.
     *
     * @param root     Node to mutate
     * @param curDepth Depth in the tree.
     * @return Returns 1 if node was added, 0 if not
     */
    protected int addNodeMutation(TreeNode root, int curDepth) {
        if (!(root instanceof InnerTreeNode)) return 0;
        //check for mutation restrictions and use local mutation settings instead of global if needed
        NodeInformation[] leafNodes;
        if (root.templateNode.canMutateTo != null) {
            if (root.templateNode.addMutationLeaf != null) leafNodes = root.templateNode.addMutationLeaf;
            else leafNodes = root.templateNode.canMutateToLeaf;
        } else leafNodes = this.leafNodes;

        if (leafNodes.length == 0) return 0;

        int newNodeIndex = rnd.nextInt(leafNodes.length);
        //check if adding new node does not violate depth constrains
        curDepth = curDepth + root.templateNode.depthWeight + leafNodes[newNodeIndex].depth;
        if (curDepth > maxTreeDepth) return 0;

        TreeNode newNode = leafNodes[newNodeIndex].template.clone();

        InnerTreeNode currentNode = (InnerTreeNode) root;
        currentNode.addNode(newNode);
        return 1;
    }

    /**
     * Mutation of nodes variables. It mutates by applying gaussian noise to numeric variable.
     *
     * @param root Node to mutate.
     */
    protected void variableMutation(TreeNode root, int i) {
        NodeInformation currentTemplate = root.templateNode;
        try {
            Object value = currentTemplate.getMethods[i].invoke(root.node);

            if (value instanceof Integer) {
                int input = (Integer) value;
                //determine minimum and maximum allowed values
                double[] minMax = getMinMaxValues(currentTemplate, input, i);

                currentTemplate.setMethods[i].invoke(root.node, getGaussianIntNoisedValue(input, (int) minMax[0], (int) minMax[1]));
            } else if (value instanceof Double) {
                double input = (Double) value;
                //determine minimum and maximum allowed values
                double[] minMax = getMinMaxValues(currentTemplate, input, i);

                currentTemplate.setMethods[i].invoke(root.node, getGaussianNoisedValue(input, minMax[0], minMax[1]));
            } else if (value instanceof Boolean) {
                boolean input = (Boolean) value;
                //25% chance to flip value
                if (rnd.nextDouble() < 0.25) {
                    currentTemplate.setMethods[i].invoke(root.node, !input);
                }
            } else if (value instanceof SelectionSetModel) {
                SelectionSetModel input = (SelectionSetModel) value;
                if (rnd.nextDouble() < 0.25) {
                    input.disableAllElements();
                    input.enableElement(rnd.nextInt(input.getStateOfElements().length));
                    currentTemplate.setMethods[i].invoke(root.node, input);
                }
            } else if (value instanceof boolean[]) {
                boolean[] input = (boolean[]) value;
                int numberOfChanges = getNumberOfChanges(input.length);
                int rndNum;
                for (int j = 0; j < numberOfChanges; j++) {
                    rndNum = rnd.nextInt(input.length);
                    input[rndNum] = !input[rndNum];
                }
                currentTemplate.setMethods[i].invoke(root.node, input);
            } else if (value instanceof double[]) {
                double[] input = doubleArrayMutation(currentTemplate, (double[]) value, i);

                currentTemplate.setMethods[i].invoke(root.node, input);
            } else if (value instanceof int[]) {
                int[] input = intArrayMutation(currentTemplate, (int[]) value, i);

                currentTemplate.setMethods[i].invoke(root.node, input);
            } else {
                //incompatible variable value
            }
        } catch (IllegalAccessException e) {
            log.warn("EXCEPTION: " + e.getMessage());
        } catch (InvocationTargetException e) {
            log.warn("EXCEPTION: " + e.getMessage());
        }
    }

    /**
     * @param currentTemplate Template of mutated object.
     * @param input           Input array that will be mutated.
     * @param varIndex        Index of variable in given object.
     * @return Returns array of 2 items minimum and maximum value (in this order) for given variable.
     */
    protected double[] getMinMaxValues(NodeInformation currentTemplate, double input, int varIndex) {
        double[] minMax = new double[2];
        if (currentTemplate.minVal == null) minMax[0] = input / defaultVarDispersion;
        else minMax[0] = currentTemplate.minVal[varIndex];

        if (currentTemplate.maxVal == null) minMax[1] = input * defaultVarDispersion;
        else minMax[1] = currentTemplate.maxVal[varIndex];
        //both values would be 0, so set maximum to 1
        if (minMax[0] == 0 && minMax[1] == 0) minMax[1] = 1;

        return minMax;
    }

    protected double[] getMinMaxArrayValues(NodeInformation currentTemplate, double[] input, int varIndex) {
        double[] minMax;
        if (currentTemplate.minVal == null || currentTemplate.maxVal == null) {
            double average = 0;
            for (int i = 0; i < input.length; i++) average += input[i];
            average = average / input.length;

            minMax = getMinMaxValues(currentTemplate, average, varIndex);
        } else {
            minMax = getMinMaxValues(currentTemplate, 1, varIndex);
        }
        return minMax;
    }

    protected int[] getMinMaxArrayValues(NodeInformation currentTemplate, int[] input, int varIndex) {
        double[] doubleMinMax;
        int[] minMax = new int[2];
        if (currentTemplate.minVal == null || currentTemplate.maxVal == null) {
            double average = 0;
            for (int i = 0; i < input.length; i++) average += input[i];
            average = average / input.length;

            doubleMinMax = getMinMaxValues(currentTemplate, average, varIndex);
        } else {
            doubleMinMax = getMinMaxValues(currentTemplate, 1, varIndex);
        }
        minMax[0] = (int) doubleMinMax[0];
        minMax[1] = (int) doubleMinMax[1];
        return minMax;
    }

    /**
     * Function will perform mutation of the given array. It first selects number of variables that will be mutated
     * (for details see function getNumberOfChanges), then performs given number of changes using selection with repetition.
     *
     * @param currentTemplate Template of mutated object.
     * @param input           Input array that will be mutated.
     * @param varIndex        Index of variable in given object.
     * @return Returns mutated array.
     */
    protected double[] doubleArrayMutation(NodeInformation currentTemplate, double[] input, int varIndex) {
        double[] minMax = getMinMaxArrayValues(currentTemplate, input, varIndex);
        //get random number of changed fields from gaussian distribution
        int numberOfChanges = getNumberOfChanges(input.length);
        int rndNum;
        for (int j = 0; j < numberOfChanges; j++) {
            rndNum = rnd.nextInt(input.length);
            input[rndNum] = getGaussianNoisedValue(input[rndNum], minMax[0], minMax[1]);
        }
        return input;
    }

    protected int[] intArrayMutation(NodeInformation currentTemplate, int[] input, int varIndex) {
        int[] minMax = getMinMaxArrayValues(currentTemplate, input, varIndex);
        //get random number of changed fields from gaussian distribution
        int numberOfChanges = getNumberOfChanges(input.length);
        int rndNum;
        for (int j = 0; j < numberOfChanges; j++) {
            rndNum = rnd.nextInt(input.length);
            input[rndNum] = getGaussianIntNoisedValue(input[rndNum], minMax[0], minMax[1]);
        }
        return input;
    }

    /**
     * @param maxValue Maximum allowed number of changes
     * @return Returns integer from <0,maxValue> using absolute values from gaussian distribution * sqrt(maxValue).
     * Meaning zero has lesser probability than 1, but from 1 it is a standard gaussian distribution. This shape is used
     * to reduce probability of no changes being made.
     */
    protected int getNumberOfChanges(int maxValue) {
        int numberOfChanges;
        do {
            numberOfChanges = (int) Math.round(rnd.nextGaussian() * Math.sqrt(maxValue));
            if (numberOfChanges < 0) numberOfChanges = Math.abs(numberOfChanges);
        } while (numberOfChanges > maxValue);
        return numberOfChanges;
    }

    /**
     * @param value Input value to which will be applied gaussian noise.
     * @param min   Minimum value of the input value.
     * @param max   Maximum value of the input value.
     * @return Returns value with gaussian noise.
     */
    protected double getGaussianNoisedValue(double value, double min, double max) {
        double output;
        if (value < min) min = value;
        if (value > max) max = value * 2;
        do {
            output = value + rnd.nextGaussian() * Math.sqrt(max - min);
        } while (output > max || output < min);
        return output;
    }

    protected int getGaussianIntNoisedValue(int value, int min, int max) {
        double output;
        if (value < min) min = value;
        if (value > max) max = value * 2;
        do {
            output = value + rnd.nextGaussian() * Math.sqrt(max - min);
            if (output > max && output < max + 0.5) output = max;
            else if (output < min && output > min - 0.5) output = min;
        } while (output > max || output < min);
        return (int) Math.round(output);
    }

    /**
     * Finds out which nodes are leaf nodes and creates array with references on them. Used for faster mutation if
     * you want to mutate to leaf node only.
     */
    protected void computeLeafNodes() {
        NodeInformation[] tmpIndexes = new NodeInformation[objectsAvailable.length];
        int idx = 0;
        for (int i = 0; i < objectsAvailable.length; i++) {
            if (!(objectsAvailable[i].template instanceof InnerTreeNode)) {
                tmpIndexes[idx] = objectsAvailable[i];
                idx++;
            }
        }
        leafNodes = new NodeInformation[idx];
        System.arraycopy(tmpIndexes, 0, leafNodes, 0, idx);
    }

    /**
     * @param root Root node.
     * @return Returns tree depth (recursive function).
     */
    protected int getTreeDepth(TreeNode root) {
        if (!(root instanceof InnerTreeNode)) return root.templateNode.depthWeight;

        InnerTreeNode tmpNode = (InnerTreeNode) root;
        int max = 0;
        int depth;
        for (int i = 0; i < tmpNode.getNodesNumber(); i++) {
            depth = getTreeDepth(tmpNode.getNode(i));
            if (depth > max) max = depth;
        }
        return max + root.templateNode.depthWeight;
    }

    protected void printEvolutionSettings() {
        log.info("-----------------------------------------------");
        log.info("EVOLUTION CONFIGURATION:");
        log.info("-----------------------------------------------");
        log.info("Context: " + fit.getClass().getSimpleName());
        log.info("Maximum number of generations: " + maxGenerations);
        log.info("Maximum tree depth: " + maxTreeDepth);
        log.info("Generation size: " + generationSize);
        if (stopIfNoChangeForNGen == Integer.MAX_VALUE)
            log.info("Number of generation to detect convergence: disabled");
        else log.info("Number of generation to detect convergence: " + stopIfNoChangeForNGen);
        log.info("Node change mutation probability: " + nodeChangeMutationProb);
        log.info("Node add mutation probability: " + nodeAddMutationProb);
        log.info("Variable mutation probability: " + variableMutationProb);
        log.info("-----------------------------------------------");
    }

    /*
     * Setr and Getr functions for algorithm settings
     */

    /**
     * Sets weight for given template. Default weights are set to 1 (ie. standard depth counting), but different values can be set
     * to make some nodes transparent(weight=0) or to imply their complexity by setting weight > 1 and preventing evolution
     * from creating trees with large number of these complex nodes.
     *
     * @param templateIndex Index of template.
     * @param weight        Weight of given template.
     */
    public void setTemplateDepthWeight(int templateIndex, int weight) {
        objectsAvailable[templateIndex].depthWeight = weight;
    }

    public void setMethodsToOptimalize(int templateIndex, String[] methodNames, Class[] variable_type) {
        Class cls;
        Method[] getMethods, setMethods;
        String firstLetter, rest;
        //cls = objectsAvailable[templateIndex].node.getClass();
        cls = objectsAvailable[templateIndex].template.node.getClass();

        getMethods = new Method[methodNames.length];
        setMethods = new Method[methodNames.length];
        for (int i = 0; i < methodNames.length; i++) {
            try {
                firstLetter = methodNames[i].substring(0, 1).toUpperCase();
                rest = methodNames[i].substring(1);
                getMethods[i] = cls.getMethod("get" + firstLetter + rest);
                setMethods[i] = cls.getMethod("set" + firstLetter + rest, variable_type[i]);
            } catch (NoSuchMethodException e) {
                log.warn("WARNING: get/set methods for variable " + methodNames[i] + " does not exist.");
                //do not optimize any methods for that object if error occurs
                getMethods = new Method[0];
                setMethods = new Method[0];
                break;
            }
        }
        objectsAvailable[templateIndex].getMethods = getMethods;
        objectsAvailable[templateIndex].setMethods = setMethods;
    }

    /**
     * Sets restrictions of add mutation for a given node. Restrictions define leaf nodes which can be created by add mutation.
     *
     * @param templateIndex Index of the template to which restrictions apply.
     * @param leafNodes     Array of leaf nodes which can be added by add mutation to given node.
     */
    public void setAddMutationLeaves(int templateIndex, FitnessNode[] leafNodes) {
        NodeInformation[] templates = getTemplateReferences(leafNodes);
        ArrayList<NodeInformation> leafTemplates = new ArrayList<NodeInformation>();
        for (int i = 0; i < templates.length; i++) {
            if (!(templates[i].template instanceof InnerTreeNode)) {
                leafTemplates.add(templates[i]);
            }
        }

        objectsAvailable[templateIndex].addMutationLeaf = leafTemplates.toArray(new NodeInformation[leafTemplates.size()]);
    }

    /**
     * Sets restriction of mutations for given node. Restrictions define nodes to which can given template mutate.
     *
     * @param templateIndex Index of the template to which restrictions apply.
     * @param canMutateTo   Array of nodes to which given template can mutate.
     */
    public void setCanMutateTo(int templateIndex, FitnessNode[] canMutateTo) {
        NodeInformation[] templates = getTemplateReferences(canMutateTo);
        ArrayList<NodeInformation> leafTemplates = new ArrayList<NodeInformation>();
        for (int i = 0; i < templates.length; i++) {
            if (!(templates[i].template instanceof InnerTreeNode)) {
                leafTemplates.add(templates[i]);
            }
        }

        objectsAvailable[templateIndex].canMutateTo = templates;
        objectsAvailable[templateIndex].canMutateToLeaf = leafTemplates.toArray(new NodeInformation[leafTemplates.size()]);
    }

    protected NodeInformation[] getTemplateReferences(FitnessNode[] nodes) {
        NodeInformation[] templates = new NodeInformation[nodes.length];

        //get IDs only once - save computation time during main cycle
        String[] templateId = new String[objectsAvailable.length];
        for (int i = 0; i < objectsAvailable.length; i++) {
            templateId[i] = objectsAvailable[i].template.node.toString();
        }

        String nodeId;
        for (int i = 0; i < templates.length; i++) {
            nodeId = nodes[i].toString();
            for (int j = 0; j < objectsAvailable.length; j++) {
                if (nodeId.equals(templateId[j])) {
                    templates[i] = objectsAvailable[j];
                    break;
                }
            }
        }
        return templates;
    }

    public void setNodeGrowingMutation(int templateIndex, boolean growingMutationEnabled) {
        objectsAvailable[templateIndex].nodeGrowingMutation = growingMutationEnabled;
    }

    /**
     * @param node Input fitness node.
     * @return Returns index of the template which represents given fitness node. Comparison is made via toString
     * representation of objects. Returns -1 if given node does not have corresponding template.
     */
    public int getTemplateIndex(FitnessNode node) {
        String nodeId = node.toString();
        for (int i = 0; i < objectsAvailable.length; i++) {
            if (nodeId.equals(objectsAvailable[i].template.node.toString())) return i;
        }
        return -1;
    }

    public void setMinVarValueForOptimalize(int templateIndex, double[] minValues) {
        objectsAvailable[templateIndex].minVal = minValues;
    }

    public void setMaxVarValueForOptimalize(int templateIndex, double[] maxValues) {
        objectsAvailable[templateIndex].maxVal = maxValues;
    }

    public void setInitGeneration(FitnessNode[] initGeneration) {
        this.initGeneration = initGeneration;
    }

    public FitnessContext getFitnessContext() {
        return fit;
    }

    public void setFitnessContext(FitnessContext fit) {
        this.fit = fit;
    }

    public TreeNode getIndividual(int index) {
        return generation[index];
    }

    public void setIndividual(int index, TreeNode individual) {
        generation[index] = individual;
        fitness[index] = avgFitness;
        age[index] = 0;
    }

    public int getGenerationSize() {
        return generationSize;
    }

    public void setGenerationSize(int generationSize) {
        if (generation != null && generation.length != generationSize) {
            TreeNode[] newGeneration = new TreeNode[generationSize];
            double[] newFitness = new double[generationSize];
            int[] newAge = new int[generationSize];
            MyRandom rndWithoutRep = new MyRandom(generation.length);
            int rnd;
            for (int i = 0; i < generationSize; i++) {
                rnd = rndWithoutRep.getRandom(generation.length);
                newGeneration[i] = generation[rnd];
                newFitness[i] = fitness[rnd];
                newAge[i] = age[rnd];
            }
            generation = newGeneration;
            fitness = newFitness;
            age = newAge;
            log.info("NEW GENERATION SIZE: " + generationSize);
        }
        this.generationSize = generationSize;
    }

    public void setMaxTreeDepth(int maxTreeDepth) {
        if (generation != null && this.maxTreeDepth != maxTreeDepth) log.info("NEW TREE DEPTH: " + maxTreeDepth);
        this.maxTreeDepth = maxTreeDepth;
    }

    public int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public void resetConvergencyCriterion() {
        noChangeForNGen = 0;
    }

    public void setNodeChangeMutationProb(double nodeChangeMutationProb) {
        this.nodeChangeMutationProb = nodeChangeMutationProb;
    }

    public double getNodeChangeMutationProb() {
        return nodeChangeMutationProb;
    }

    public void setNodeAddMutationProb(double nodeAddMutationProb) {
        this.nodeAddMutationProb = nodeAddMutationProb;
    }

    public double getNodeAddMutationProb() {
        return nodeAddMutationProb;
    }

    public void setVariableMutationProb(double variableMutationProb) {
        this.variableMutationProb = variableMutationProb;
    }

    public double getVariableMutationProb() {
        return variableMutationProb;
    }

    public void setConvergencyCriterion(int stopIfNoChangeForNGen) {
        this.stopIfNoChangeForNGen = stopIfNoChangeForNGen;
    }

    public int getConvergencyCriterion() {
        return stopIfNoChangeForNGen;
    }

    public void setDefaultDispersion(double variableDispersion) {
        defaultVarDispersion = variableDispersion;
    }

    public double getDefaultDispersion() {
        return defaultVarDispersion;
    }

    public FitnessNode getTemplate(int templateIndex) {
        return objectsAvailable[templateIndex].template.node;
    }

    public TreeNode getTreeNodeTemplate(int templateIndex) {
        return objectsAvailable[templateIndex].template;
    }

    public int getNumberOfTemplates() {
        return objectsAvailable.length;
    }

    public void setLocalMutationThreshold(double localMutationThreshold) {
        this.localMutationThreshold = localMutationThreshold;
    }

    public void setOutputFitnessFormat(boolean showFitness) {
        if (showFitness) this.showFitness = 1;
        else this.showFitness = -1;
    }

}
