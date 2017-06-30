package game.classifiers.single;

import configuration.CfgTemplate;
import configuration.models.ensemble.BaseModelsDefinition;
import game.evolution.treeEvolution.context.InterruptibleArrayList;
import game.models.ModelLearnable;
import game.models.Model;
import game.models.ensemble.ModelEnsemble;
import game.utils.MyRandom;
import game.classifiers.ClassifierBase;
import game.classifiers.ClassifierSingle;

import java.util.ArrayList;
import java.util.List;

import configuration.classifiers.single.ClassifierModelConfig;
import configuration.classifiers.ClassifierConfig;
import configuration.models.ModelConfig;

/**
 * Implementation of classifier which encapsulates models for classification. Represents leaf node in classifier tree.
 * Author: cernyjn
 */
public class ClassifierModel extends ClassifierBase implements ClassifierSingle {
    protected ArrayList<Model> classifierModels;
    protected int numModels;
    ClassifierModelConfig classifierCfg;

    /**
     * Pass data to LearnableModel from inputVect chosen by selection without repetition.
     *
     * @param LearnableModel
     */
    private void prepareData(ModelLearnable LearnableModel) {
        LearnableModel.resetLearningData();

        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min;
        //pass minimum from avaliable vectors and maxLearningVectors of classifierModel
        if (LearnableModel.getMaxLearningVectors() > learning_vectors) min = learning_vectors;
        else min = LearnableModel.getMaxLearningVectors();

        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            LearnableModel.storeLearningVector(inputVect[rnd], target[rnd][LearnableModel.getTargetVariable()]);
        }
    }

    /**
     * If LearnerModel is ensemble it calls relearn to ensure all models in that ensemble will be relearned.
     *
     * @param LearnableModel
     */
    private void relearnModel(ModelLearnable LearnableModel) {
        if (LearnableModel instanceof ModelEnsemble) {
            ModelEnsemble ensModel = (ModelEnsemble) LearnableModel;
            ensModel.relearn();
        } else {
            LearnableModel.learn();
        }
    }

    /**
     * Check if all models are learned, if so set learned=true;
     */
    private void checkLearned() {
        if (learned) return;
        ModelLearnable LearnableModel;
        for (int i = 0; i < numModels; i++) {
            if (classifierModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) classifierModels.get(i);
                //if there is a non learned model return
                if (!LearnableModel.isLearned()) return;
            }
        }
        learned = true;
    }

    public void setMaxLearningVectors(int maxVectors) {
        super.setMaxLearningVectors(maxVectors);
        for (int i = 0; i < numModels; i++) {
            if (classifierModels.get(i) instanceof ModelLearnable) {
                ((ModelLearnable) classifierModels.get(i)).setMaxLearningVectors(maxVectors);
            }
        }
    }

    @Override
    public Class getConfigClass() {
        return ClassifierModelConfig.class;
    }

    public void init(ClassifierConfig cfg) {
        classifierModels = new InterruptibleArrayList<Model>();
        classifierCfg = (ClassifierModelConfig) cfg;

        super.init(cfg);
    }

    public void storeLearningVector(double[] input, double[] output) {
        super.storeLearningVector(input, output);

        if (classifierCfg != null) {
            numModels = outputs;
            createClassModels(classifierCfg);
            classifierCfg = null;
        }
    }

    /**
     * Creates model of the class <index> using the class name and the configuration template
     *
     * @param index  the target variable
     * @param config the configuration template
     */
    protected void createClassModel(int index, CfgTemplate config) {
        ModelLearnable learnable;
        try {
            Class m = config.getClassRef();
            learnable = (ModelLearnable) m.newInstance();
            ModelConfig cfg = (ModelConfig) config;

            learnable.init(cfg);
            learnable.setTargetVariable(index); //force right value of the target variable
            learnable.setMaxLearningVectors(maxLearningVectors);
            classifierModels.add(learnable);
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Creates class models according to their configuration
     * - PREDEFINED: all class models have their own configuration bean - added to the list baseModelCfgs
     * - RANDOM: classModelCfgs contains cfg beans of all models implemented so far. Class models are randomly selected from this list respecting the allowed flag
     * - UNIFORM: classModelCfgs contains only one configuration bean, all generated models are of the same type
     * - UNIFORM_RANDOM: classModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
     */
    protected void createClassModels(ClassifierModelConfig cfg) {
        List<CfgTemplate> listCfg = cfg.getClassModelCfgs();
        MyRandom rndGenerator = new MyRandom(listCfg.size());
        switch (cfg.getClassModelsDef()) {
            case PREDEFINED:
                if (outputs != listCfg.size()) {
                    randomClassModelDistribution(listCfg, rndGenerator);
                } else {
                    for (int i = 0; i < outputs; i++) {
                        createClassModel(i, listCfg.get(i));
                    }
                }
                break;
            case RANDOM:
                randomClassModelDistribution(listCfg, rndGenerator);
                break;
            case UNIFORM:
                for (int i = 0; i < outputs; i++)
                    createClassModel(i, listCfg.get(0));
                break;
            case UNIFORM_RANDOM:
                int rnd = rndGenerator.nextInt(listCfg.size());
                for (int i = 0; i < outputs; i++)
                    createClassModel(i, listCfg.get(rnd));
                break;
        }

    }

    private void randomClassModelDistribution(List<CfgTemplate> listCfg, MyRandom rndGenerator) {
        for (int i = 0; i < outputs; i++) {
            createClassModel(i, listCfg.get(rndGenerator.nextInt(listCfg.size())));
        }
    }

    public void learn() {
        ModelLearnable LearnableModel;
        for (int i = 0; i < numModels; i++) {
            if (classifierModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) classifierModels.get(i);
                //learn model if its not already learned
                if (!LearnableModel.isLearned()) {
                    prepareData(LearnableModel);
                    LearnableModel.learn();
                }
            }
        }
        learned = true;
    }

    public void relearn() {
        ModelLearnable LearnableModel;
        for (int i = 0; i < numModels; i++) {
            if (classifierModels.get(i) instanceof ModelLearnable) {
                LearnableModel = (ModelLearnable) classifierModels.get(i);
                prepareData(LearnableModel);
                relearnModel(LearnableModel);
            }
        }
        learned = true;
    }

    public void learn(int modelIndex) {
        if (classifierModels.get(modelIndex) instanceof ModelLearnable) {
            ModelLearnable LearnableModel = (ModelLearnable) classifierModels.get(modelIndex);
            prepareData(LearnableModel);
            relearnModel(LearnableModel);
            checkLearned();
        }
    }

    public ClassifierConfig getConfig() {
        ClassifierModelConfig cfg = (ClassifierModelConfig) super.getConfig();
        cfg.setModelsNumber(numModels);
        cfg.setClassModelsDef(BaseModelsDefinition.PREDEFINED);
        for (int i = 0; i < numModels; i++) {
            cfg.addClassModelCfg(classifierModels.get(i).getConfig());
        }
        return cfg;
    }

    public Model getModel(int modelIndex) {
        return classifierModels.get(modelIndex);
    }

    public Model[] getAllModels() {
        return classifierModels.toArray(new Model[0]);
    }

    public void setModel(int modelIndex, Model model) {
        classifierModels.set(modelIndex, model);
        learned = false;
    }

    public void addModel(int modelIndex, Model model) {
        classifierModels.add(modelIndex, model);
        numModels++;
        learned = false;
    }

    public void removeModel(int modelIndex) {
        classifierModels.remove(modelIndex);
        numModels--;
        learned = false;
    }

    public int getOutput(double[] input_vector) {
        if (!learned) learn();
        double max = classifierModels.get(0).getOutput(input_vector);
        int chosenAttribute = classifierModels.get(0).getTargetVariable();
        double modelOutput;

        for (int i = 1; i < numModels; i++) {
            modelOutput = classifierModels.get(i).getOutput(input_vector);
            if (modelOutput > max) {
                max = modelOutput;
                chosenAttribute = classifierModels.get(i).getTargetVariable();
            }
        }
        return chosenAttribute;
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] output = new double[outputs];
        double sum = 0;

        for (int i = 0; i < numModels; i++) {
            output[classifierModels.get(i).getTargetVariable()] += classifierModels.get(i).getOutput(input_vector);
            //transfer negative numbers to 0
            if (output[i] < 0) output[i] = 0;
            if (output[i] > 1) output[i] = 1;
            sum += output[i];
        }
        //normalize values
        if (sum != 0) {
            for (int i = 0; i < numModels; i++) {
                output[i] = output[i] / sum;
            }
        }
        return output;
    }

    public String[] getEquations(String[] inputEquation) {
        if (!learned) learn();
        String output[] = new String[outputs];
        Model model;

        for (int i = 0; i < numModels; i++) {
            model = classifierModels.get(i);
            output[model.getTargetVariable()] = model.toEquation(inputEquation);
        }
        return output;
    }

    public void deleteLearningVectors() {
        super.deleteLearningVectors();
        for (int i = 0; i < numModels; i++) {
            if (classifierModels.get(i) instanceof ModelLearnable) {
                ModelLearnable model = (ModelLearnable) classifierModels.get(i);
                model.deleteLearningVectors();
            }
        }
    }

}
