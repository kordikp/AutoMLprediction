package game.models.ensemble;

import configuration.CfgTemplate;
import game.evolution.treeEvolution.context.InterruptibleArrayList;
import game.models.Model;
import game.models.ModelLearnable;
import game.models.ModelLearnableBase;
import game.utils.MyRandom;

import java.util.ArrayList;
import java.util.List;

import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.EnsembleModelConfig;

/**
 * Abstract class for base ensemble methods
 * Author: cernyjn
 */
public abstract class ModelEnsembleBase extends ModelLearnableBase implements ModelEnsemble {
    protected ArrayList<? extends Model> ensembleModels;
    List<CfgTemplate> baseModelsCfg;
    BaseModelsDefinition baseModelsDef;
    protected int modelsNumber;


    public void init(ModelConfig cfg) {
        baseModelsCfg = ((EnsembleModelConfig) cfg).getBaseModelCfgs();
        baseModelsDef = ((EnsembleModelConfig) cfg).getBaseModelsDef();

        modelsNumber = ((EnsembleModelConfig) cfg).getModelsNumber();
        ensembleModels = new InterruptibleArrayList<Model>(modelsNumber);
        //initialize base model structures
        super.init(cfg);

        createBaseModels();
    }

    /**
     * Creates model instance from config and saves it into ensemble to given position. Also maintain certain integrity
     * in model structures and forces certain values to metamodels.
     *
     * @param position Position in ensembleModels to save model.
     * @param config   Class name with config.
     */
    protected void addBaseModel(int position, CfgTemplate config) {
        ModelLearnable learnable;
        try {
            Class m = config.getClassRef();
            learnable = (ModelLearnable) m.newInstance();
            ModelConfig cfg = (ModelConfig) config;
            learnable.init(cfg);
            ((ArrayList<Model>) ensembleModels).add(position, learnable);
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Creates ensemble models according to their configuration
     * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
     * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
     * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
     * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
     */
    protected void createBaseModels() {
        MyRandom rndGenerator = new MyRandom(baseModelsCfg.size());
        int initModelsNumber = modelsNumber;
        switch (baseModelsDef) {
            case PREDEFINED:
                modelsNumber = baseModelsCfg.size();
                for (int i = 0; i < modelsNumber; i++) {
                    addBaseModel(i, baseModelsCfg.get(i));
                }
                break;
            case RANDOM:
                for (int i = 0; i < initModelsNumber; i++)
                    addBaseModel(i, baseModelsCfg.get(rndGenerator.nextInt(baseModelsCfg.size())));
                break;
            case UNIFORM:
                for (int i = 0; i < initModelsNumber; i++)
                    addBaseModel(i, baseModelsCfg.get(0));
                break;
            case UNIFORM_RANDOM:
                int rnd = rndGenerator.nextInt(baseModelsCfg.size());
                for (int i = 0; i < initModelsNumber; i++)
                    addBaseModel(i, baseModelsCfg.get(rnd));
                break;
        }

    }

    public ModelConfig getConfig() {
        EnsembleModelConfig cfg = (EnsembleModelConfig) super.getConfig();
        cfg.setModelsNumber(modelsNumber);
        cfg.setBaseModelsDef(BaseModelsDefinition.PREDEFINED);
        for (int i = 0; i < modelsNumber; i++) {
            cfg.addBaseModelCfg(ensembleModels.get(i).getConfig());
        }
        return cfg;
    }

    protected void relearnModel(ModelLearnable LearnableModel) {
        try { //find out which models are ensembled
            ModelEnsemble ensModel = (ModelEnsemble) LearnableModel;
            ensModel.relearn();
        } catch (ClassCastException e) {
            LearnableModel.learn();
        }
    }

    public void setMaxLearningVectors(int maxVectors) {
        super.setMaxLearningVectors(maxVectors);
        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                ((ModelLearnable) ensembleModels.get(i)).setMaxLearningVectors(maxVectors);
            }
        }
    }

    public Model getModel(int modelIndex) {
        return ensembleModels.get(modelIndex);
    }

    public void setModel(int modelIndex, Model model) {
        ((ArrayList<Model>) ensembleModels).set(modelIndex, model);
        learned = false;          //todo save type cast?
    }

    @Override
    public void appendModel(Model model) {
        ((ArrayList<Model>) ensembleModels).add(model);
        modelsNumber++;
        learned = false;
    }

    public void addModel(int modelIndex, Model model) {
        ((ArrayList<Model>) ensembleModels).add(modelIndex, model);
        modelsNumber++;
        learned = false;
    }

    public void removeModel(int modelIndex) {
        ensembleModels.remove(modelIndex);
        modelsNumber--;
        learned = false;
    }

    public void deleteLearningVectors() {
        learning_vectors = 0;
        inputVect = new double[0][0];
        target = new double[0];

        for (int i = 0; i < modelsNumber; i++) {
            if (ensembleModels.get(i) instanceof ModelLearnable) {
                ModelLearnable model = (ModelLearnable) ensembleModels.get(i);
                model.deleteLearningVectors();
            }
        }
    }

    public String getTrainedBy() {
        return this.getClass().getName();
    }

    public void setTargetVariable(int targetVariable) {
        for (int i = 0; i < modelsNumber; i++) {
            ensembleModels.get(i).setTargetVariable(targetVariable);
        }
        this.targetVariable = targetVariable;
    }

    public int getModelsNumber() {
        return modelsNumber;
    }

}
