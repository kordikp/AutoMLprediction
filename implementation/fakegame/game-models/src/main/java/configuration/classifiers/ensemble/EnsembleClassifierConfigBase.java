package configuration.classifiers.ensemble;

import configuration.CfgTemplate;
import configuration.classifiers.ClassifierConfig;
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
@Component(name = "EnsembleClassifierConfig", description = "Configuration of classifiers ensemble")
public abstract class EnsembleClassifierConfigBase extends ClassifierConfigBase implements EnsembleClassifierConfig, InnerFitnessNode {

    @Property(name = "Number of classifiers", description = "Total number of classifiers to be generated and combined")
    protected int classifiersNumber;

    @Property(name = "Base classifiers can be configured in the following manner", description = "Various schemes to configure base classifiers")
    protected BaseModelsDefinition baseClassifiersDef;
    protected List<CfgTemplate> baseClassifiersCfgs;


    public EnsembleClassifierConfigBase clone() {
        EnsembleClassifierConfigBase newObject;
        newObject = (EnsembleClassifierConfigBase) super.clone();
        if (baseClassifiersDef != null) newObject.baseClassifiersDef = baseClassifiersDef;
        if (baseClassifiersCfgs != null) {
            newObject.baseClassifiersCfgs = new ArrayList<CfgTemplate>();
            for (int i = 0; i < baseClassifiersCfgs.size(); i++) {
                newObject.baseClassifiersCfgs.add(baseClassifiersCfgs.get(i).clone());
            }
        }
        return newObject;
    }

    public void setNode(int index, FitnessNode node) {
        if (node instanceof ClassifierConfig) {
            baseClassifiersCfgs.set(index, (ClassifierConfig) node);
        }
    }

    public FitnessNode getNode(int index) {
        return (FitnessNode) baseClassifiersCfgs.get(index);
    }

    public void addNode(FitnessNode node) {
        if (node instanceof ClassifierConfig) {
            baseClassifiersCfgs.add((ClassifierConfig) node);
        }
    }

    public int getNodesNumber() {
        return baseClassifiersCfgs.size();
    }

    public void removeNode(int index) {
        baseClassifiersCfgs.remove(index);
    }

    public void removeNode(FitnessNode node) {
        baseClassifiersCfgs.remove(node);
    }

    public String toString() {
        String output = super.toString() + "{" + classifiersNumber + "x ";
        for (int i = 0; i < baseClassifiersCfgs.size(); i++) {
            ClassifierConfig cfg = (ClassifierConfig) baseClassifiersCfgs.get(i);
            output += cfg.toString() + ",";
        }
        output = output.substring(0, output.length() - 1);
        output += "}";
        return output;
    }

    /**
     * Number of member models in ensemble.
     */
    public int getClassifiersNumber() {
        return classifiersNumber;
    }

    public void setClassifiersNumber(int classifiersNumber) {
        if (classifiersNumber < 2) this.classifiersNumber = 2;
        else this.classifiersNumber = classifiersNumber;
    }


    public List<CfgTemplate> getBaseClassifiersCfgs() {
        return baseClassifiersCfgs;
    }

    public void setBaseClassifiersCfgs(List<CfgTemplate> baseClassifiersCfgs) {
        this.baseClassifiersCfgs = baseClassifiersCfgs;
    }

    public void addBaseClassifierCfg(CfgTemplate baseClassifierCfg) {
        this.baseClassifiersCfgs.add(baseClassifierCfg);
    }

    public BaseModelsDefinition getBaseClassifiersDef() {
        return baseClassifiersDef;
    }

    public void setBaseClassifiersDef(BaseModelsDefinition baseClassifiersDef) {
        this.baseClassifiersDef = baseClassifiersDef;
    }


    /**
     * Constructor with default values;
     */
    public EnsembleClassifierConfigBase() {
        baseClassifiersCfgs = new ArrayList<CfgTemplate>();
        baseClassifiersDef = BaseModelsDefinition.RANDOM;
        classifiersNumber = 5;
    }

}