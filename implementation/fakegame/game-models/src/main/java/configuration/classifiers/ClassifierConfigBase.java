package configuration.classifiers;

import configuration.AbstractCfgBean;
import game.evolution.treeEvolution.FitnessNode;
import org.ytoh.configurations.annotations.Property;

/**
 * Abstract class for config methods
 * Author: cernyjn
 */
public abstract class ClassifierConfigBase extends AbstractCfgBean implements ClassifierConfig, FitnessNode {
    @Property(name = "Maximum learning vectors", description = "Limit the number of learning vectors used.")
    protected int maxLearningVectors;

    @Property(name = "Maximum inputs number", description = "Limit the number of input attributes used in model.")
    protected int maxInputsNumber;

    @Property(name = "Model name")
    protected String name;

    /**
     * Constructor with default values.
     */
    public ClassifierConfigBase() {
        maxLearningVectors = -1;
        maxInputsNumber = -1;
    }

    public ClassifierConfigBase clone() {
        ClassifierConfigBase newObject;
        newObject = (ClassifierConfigBase) super.clone();
        return newObject;
    }

    /**
     * Maximum number of learning vectors used.
     */
    public int getMaxLearningVectors() {
        return maxLearningVectors;
    }

    public void setMaxLearningVectors(int numberOfVectors) {
        maxLearningVectors = numberOfVectors;
    }


    /**
     * Name of the model.
     */
    //   protected String name;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Output variable for which is model learned for.
     */
//    protected int targetVariable;
    public int getMaxInputsNumber() {
        return maxInputsNumber;
    }

    public void setMaxInputsNumber(int maxInputsNumber) {
        this.maxInputsNumber = maxInputsNumber;
    }

}