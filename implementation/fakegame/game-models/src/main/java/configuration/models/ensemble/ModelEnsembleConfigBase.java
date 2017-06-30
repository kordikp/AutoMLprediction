package configuration.models.ensemble;

import configuration.CfgTemplate;
import configuration.models.ModelConfigBase;
import configuration.models.ModelConfig;
import game.evolution.treeEvolution.InnerFitnessNode;
import game.evolution.treeEvolution.FitnessNode;

import java.util.List;
import java.util.ArrayList;

import org.ytoh.configurations.annotations.Property;

/**
 * Abstract class for ensemble config methods
 * Author: cernyjn
 */
public abstract class ModelEnsembleConfigBase extends ModelConfigBase implements EnsembleModelConfig, InnerFitnessNode {
    @Property(name = "Number of models", description = "Total number of models to be generated and combined")
    protected int modelsNumber;

    @Property(name = "Base models can be configured in the following manner", description = "Various schemes to configure base models")
    protected BaseModelsDefinition baseModelsDef;
    protected List<CfgTemplate> baseModelCfgs;

    /**
     * Constructor with default values;
     */
    public ModelEnsembleConfigBase() {
        super();
        baseModelCfgs = new ArrayList<CfgTemplate>();
        baseModelsDef = BaseModelsDefinition.RANDOM;
        modelsNumber = 5;
    }

    public ModelEnsembleConfigBase clone() {
        ModelEnsembleConfigBase newObject;
        newObject = (ModelEnsembleConfigBase) super.clone();
        if (baseModelsDef != null) newObject.baseModelsDef = baseModelsDef;
        if (baseModelCfgs != null) {
            newObject.baseModelCfgs = new ArrayList<CfgTemplate>();
            for (int i = 0; i < baseModelCfgs.size(); i++) {
                newObject.baseModelCfgs.add(baseModelCfgs.get(i).clone());
            }
        }
        return newObject;
    }

    public void setNode(int index, FitnessNode node) {
        if (node instanceof ModelConfig) {
            baseModelCfgs.set(index, (ModelConfig) node);
        }
    }

    public FitnessNode getNode(int index) {
        return (FitnessNode) baseModelCfgs.get(index);
    }

    public void addNode(FitnessNode node) {
        if (node instanceof ModelConfig) {
            baseModelCfgs.add((ModelConfig) node);
        }
    }

    public int getNodesNumber() {
        return baseModelCfgs.size();
    }

    public void removeNode(int index) {
        baseModelCfgs.remove(index);
    }

    public void removeNode(FitnessNode node) {
        baseModelCfgs.remove(node);
    }

    public String toString() {
        String output = super.toString() + "[" + modelsNumber + "x ";
        for (int i = 0; i < baseModelCfgs.size(); i++) {
            ModelConfig cfg = (ModelConfig) baseModelCfgs.get(i);
            output += cfg.toString() + ",";
        }
        output = output.substring(0, output.length() - 1);
        output += "]";
        return output;
    }

    /**
     * Number of member models in ensemble.
     */
    public int getModelsNumber() {
        return modelsNumber;
    }

    public void setModelsNumber(int modelsNumber) {
        if (modelsNumber < 2) this.modelsNumber = 2;
        else this.modelsNumber = modelsNumber;
    }


    public List<CfgTemplate> getBaseModelCfgs() {
        return baseModelCfgs;
    }

    public void setBaseModelCfgs(List<CfgTemplate> baseModelCfgs) {
        this.baseModelCfgs = baseModelCfgs;
    }

    public void addBaseModelCfg(CfgTemplate baseModelCfg) {
        this.baseModelCfgs.add(baseModelCfg);
    }

    public BaseModelsDefinition getBaseModelsDef() {
        return baseModelsDef;
    }

    public void setBaseModelsDef(BaseModelsDefinition baseModelsDef) {
        this.baseModelsDef = baseModelsDef;
    }
}
