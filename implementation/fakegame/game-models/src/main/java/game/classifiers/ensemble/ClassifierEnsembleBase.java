package game.classifiers.ensemble;

import configuration.CfgTemplate;
import configuration.classifiers.ensemble.EnsembleClassifierConfig;
import game.classifiers.ClassifierBase;
import game.classifiers.Classifier;
import game.evolution.treeEvolution.context.InterruptibleArrayList;
import game.utils.MyRandom;

import java.util.ArrayList;
import java.util.List;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.EnsembleClassifierConfigBase;
import configuration.models.ensemble.BaseModelsDefinition;

/**
 * Abstract class for commonly used ensemble classifiers methods
 * Author: cernyjn
 */
public abstract class ClassifierEnsembleBase extends ClassifierBase implements ClassifierEnsemble {
    protected ArrayList<? extends Classifier> ensClassifiers;
    protected int numClassifiers;
    protected BaseModelsDefinition baseClassifiersDef;
    protected List<CfgTemplate> baseClassifiersCfgs;


    public void init(ClassifierConfig cfg) {
        //todo: k cemu to tam ukladame?
        baseClassifiersCfgs = ((EnsembleClassifierConfigBase) cfg).getBaseClassifiersCfgs();
        baseClassifiersDef = ((EnsembleClassifierConfigBase) cfg).getBaseClassifiersDef();

        numClassifiers = ((EnsembleClassifierConfigBase) cfg).getClassifiersNumber();
        ensClassifiers = new InterruptibleArrayList<Classifier>(numClassifiers);
        //initialize base Classifier structures
        super.init(cfg);

        createBaseClassifiers();
    }

    public int getClasifiersNumber() {
        return ensClassifiers.size();
    }

    protected void addBaseClassifier(int position, CfgTemplate config) {
        try {
            Classifier m;
            m = (Classifier) config.getClassRef().newInstance();
            m.init((ClassifierConfig) config);
            ((ArrayList<Classifier>) ensClassifiers).add(position, m);
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Creates ensemble Classifiers according to their configuration
     * - PREDEFINED: all base Classifiers have their own configuration bean - added to the list baseClassifierCfgs
     * - RANDOM: baseClassifierCfgs contains cfg beans of all Classifiers implemented so far. Base Classifiers are randomly selected from this list respecting the allowed flag
     * - UNIFORM: baseClassifierCfgs contains only one configuration bean, all generated Classifiers are of the same type
     * - UNIFORM_RANDOM: baseClassifierCfgs contains cfg beans of all Classifiers implemented so far, one cfg bean is randomly selected and all Classifiers are of this type
     */
    protected void createBaseClassifiers() {
        MyRandom rndGenerator = new MyRandom(baseClassifiersCfgs.size());
        switch (baseClassifiersDef) {
            case PREDEFINED:
                numClassifiers = baseClassifiersCfgs.size();
                for (int i = 0; i < numClassifiers; i++) {
                    addBaseClassifier(i, baseClassifiersCfgs.get(i));
                }
                break;
            case RANDOM:
                for (int i = 0; i < numClassifiers; i++)
                    addBaseClassifier(i, baseClassifiersCfgs.get(rndGenerator.nextInt(baseClassifiersCfgs.size())));
                break;
            case UNIFORM:
                for (int i = 0; i < numClassifiers; i++)
                    addBaseClassifier(i, baseClassifiersCfgs.get(0));
                break;
            case UNIFORM_RANDOM:
                int rnd = rndGenerator.nextInt(baseClassifiersCfgs.size());
                for (int i = 0; i < numClassifiers; i++)
                    addBaseClassifier(i, baseClassifiersCfgs.get(rnd));
                break;
        }

    }

    public ClassifierConfig getConfig() {
        EnsembleClassifierConfig cfg = (EnsembleClassifierConfig) super.getConfig();
        cfg.setClassifiersNumber(numClassifiers);
        cfg.setBaseClassifiersDef(BaseModelsDefinition.PREDEFINED);
        for (int i = 0; i < numClassifiers; i++) {
            cfg.addBaseClassifierCfg(ensClassifiers.get(i).getConfig());
        }
        return cfg;
    }

    public void setMaxLearningVectors(int maxVectors) {
        super.setMaxLearningVectors(maxVectors);
        for (int i = 0; i < numClassifiers; i++) {
            ensClassifiers.get(i).setMaxLearningVectors(maxVectors);
        }
    }

    public Classifier getClassifier(int ClassifierIndex) {
        return ensClassifiers.get(ClassifierIndex);
    }

    public void setClassifier(int ClassifierIndex, Classifier classifier) {
        ((ArrayList<Classifier>) ensClassifiers).set(ClassifierIndex, classifier);
        learned = false;
    }

    public void addClassifier(int ClassifierIndex, Classifier classifier) {
        ((ArrayList<Classifier>) ensClassifiers).add(ClassifierIndex, classifier);
        numClassifiers++;
        learned = false;
    }

    public void removeClassifier(int ClassifierIndex) {
        ensClassifiers.remove(ClassifierIndex);
        numClassifiers--;
        learned = false;
    }

    public void deleteLearningVectors() {
        super.deleteLearningVectors();
        for (int i = 0; i < numClassifiers; i++) {
            ensClassifiers.get(i).deleteLearningVectors();
        }
    }
}
