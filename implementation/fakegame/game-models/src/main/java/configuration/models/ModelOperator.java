package configuration.models;

import configuration.CfgTemplate;
import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.InnerFitnessNode;

/**
 * Abstract class for model operators - objects encapsulating one model and manipulating data
 */
public abstract class ModelOperator extends ModelConfigBase implements InnerFitnessNode {
    protected CfgTemplate config;

    public ModelOperator clone() {
        ModelOperator newObject;
        newObject = (ModelOperator) super.clone();
        if (config != null) newObject.config = config.clone();
        return newObject;
    }

    public ModelOperator() {
        config = null;
    }

    public FitnessNode getNode(int index) {
        if (index == 0) return (FitnessNode) config;
        else return null;
    }

    public void setNode(int index, FitnessNode node) {
        if (index == 0) config = (CfgTemplate) node;
    }

    public void addNode(FitnessNode node) {
        if (config == null) config = (CfgTemplate) node;
    }

    public int getNodesNumber() {
        if (config == null) return 0;
        else return 1;
    }

    public void removeNode(int index) {
        if (index == 0) config = null;
    }

    public void removeNode(FitnessNode node) {
        if (node == config) config = null;
    }

    public String toString() {
        String output = super.toString() + "|";
        if (config != null) output += config.toString();
        output += "|";
        return output;
    }
}
