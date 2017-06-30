package configuration.classifiers.single;

import configuration.CfgTemplate;
import configuration.models.ModelConfig;
import configuration.models.ModelConfigBase;
import game.classifiers.single.ClassifierModel;
import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.InnerFitnessNode;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.classifiers.ClassifierConfigBase;


import java.util.List;
import java.util.ArrayList;

/**
 * Configures a classifier. Our classifier has one model for each class. These class models has to be configured using a list in this bean. There are more definitions how the configuration can look like:
 * when classModelsDef is
 * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
 * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
 * - EVOLVED: the same as random, just allowed models are evolved by genetic algorithm to find best ensemble
 * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
 * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
 */
@Component(name = "ClassifierModelConfig", description = "Configuration of a classifier - one model for each class")
public class ClassifierModelConfig extends ClassifierConfigBase implements InnerFitnessNode {
    //do not optimize
    protected transient int modelsNumber;

    @Property(name = "Class models can be configured in the following manner", description = "Various schemes to configure class models")
    protected BaseModelsDefinition baseModelsDef;
    protected List<CfgTemplate> baseModelCfgs;

    public ClassifierModelConfig clone() {
        ClassifierModelConfig newObject;
        newObject = (ClassifierModelConfig) super.clone();
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
        if (node instanceof ModelConfigBase) {
            ModelConfigBase cfg = (ModelConfigBase) node;
            baseModelCfgs.set(index, cfg);
        }
    }

    public FitnessNode getNode(int index) {
        return (FitnessNode) baseModelCfgs.get(index);
    }

    public void addNode(FitnessNode node) {
        if (node instanceof ModelConfigBase) {
            ModelConfigBase cfg = (ModelConfigBase) node;
            baseModelCfgs.add(cfg);
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
        String output = super.toString() + "{<outputs>x ";
        for (int i = 0; i < baseModelCfgs.size(); i++) {
            ModelConfig cfg = (ModelConfig) baseModelCfgs.get(i);
            output += cfg.toString() + ",";
        }
        output = output.substring(0, output.length() - 1);
        output += "}";
        return output;
    }

    /**
     * Constructor with default values;
     */
    public ClassifierModelConfig() {
        baseModelCfgs = new ArrayList<CfgTemplate>();
        baseModelsDef = BaseModelsDefinition.RANDOM;
        classRef = ClassifierModel.class;
    }

    public BaseModelsDefinition getClassModelsDef() {
        return baseModelsDef;
    }

    public void setClassModelsDef(BaseModelsDefinition classModelsDef) {
        this.baseModelsDef = classModelsDef;
    }

    public List<CfgTemplate> getClassModelCfgs() {
        return baseModelCfgs;
    }

    public void setClassModelCfgs(List<CfgTemplate> classModelCfgs) {
        this.baseModelCfgs = classModelCfgs;
    }

    public void addClassModelCfg(CfgTemplate baseModelCfg) {
        this.baseModelCfgs.add(baseModelCfg);
    }

    public int getModelsNumber() {
        return modelsNumber;
    }

    public void setModelsNumber(int modelsNumber) {
        this.modelsNumber = modelsNumber;
    }
}