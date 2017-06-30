package configuration.models;

import configuration.AbstractCfgBean;
import game.evolution.treeEvolution.FitnessNode;
import org.ytoh.configurations.annotations.Property;

import java.io.Serializable;

/**
 * Abstract class for config methods
 * Author: cernyjn
 */
public abstract class ModelConfigBase extends AbstractCfgBean implements ModelConfig, FitnessNode, Serializable {
    @Property(name = "Maximum learning vectors", description = "Limit the number of learning vectors used.")
    protected int maxLearningVectors;

    @Property(name = "Maximum inputs number", description = "Limit the number of input attributes used in model.")
    protected int maxInputsNumber;

    @Property(name = "Model name")
    protected String name;

    //@Property(name = "Output variable for which model is learned")
    //note that this variable is not set by user,
    // but configured by the application at the time a model is constructed
    protected transient int targetVariable;

    public ModelConfigBase clone() {
        ModelConfigBase newObject;
        newObject = (ModelConfigBase) super.clone();
        return newObject;
    }

    /**
     * Constructor with default values.
     */
    public ModelConfigBase() {
        super();
        maxLearningVectors = -1;
        maxInputsNumber = -1;
        targetVariable = 0;
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

    public int getTargetVariable() {
        return targetVariable;
    }

    public void setTargetVariable(int targetVariable) {
        this.targetVariable = targetVariable;
    }
}
