package game.models.ensemble;

import configuration.CfgTemplate;
import game.evolution.Dna;
import game.evolution.EvolutionContext;
import game.evolution.EvolutionStrategy;
import game.evolution.Genome;
import game.evolution.ObjectEvolvable;
import game.models.Model;
import game.models.ModelLearnable;
import game.models.evolution.EvolvableModel;
import game.models.evolution.ModelEvolvable;
import game.utils.MyRandom;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import configuration.models.ModelConfig;
import configuration.models.ensemble.EvolvableEnsembleModelConfig;

/**
 * Model ensemble evolver for population of models represented by Genome
 */
public class ModelEvolvableEnsemble extends ModelEnsembleBase implements EvolutionContext {
    static Logger logger = Logger.getLogger(ModelEvolvableEnsemble.class);
    protected MyRandom rndGenerator;
    protected int generations;
    EvolutionStrategy evolution;
    int maxInputs;
    int learnValidRatio;
    int learnVectNum;  //learning data indexes
    int validVectNum;  //validation data indexes
    boolean genomeDistEnabled;
    boolean corrDistEnabled;
    boolean outputDistEnabled;


    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        learnValidRatio = ((EvolvableEnsembleModelConfig) cfg).getLearnValidRatio();
        generations = ((EvolvableEnsembleModelConfig) cfg).getGenerations();
        modelsNumber = ((EvolvableEnsembleModelConfig) cfg).getModelsNumber();
        genomeDistEnabled = ((EvolvableEnsembleModelConfig) cfg).isGenoDistanceEnabled();
        corrDistEnabled = ((EvolvableEnsembleModelConfig) cfg).isCorrelationDistanceEnabled();
        outputDistEnabled = ((EvolvableEnsembleModelConfig) cfg).isOutputsDistanceEnabled();

        try {
            evolution = (EvolutionStrategy) ((EvolvableEnsembleModelConfig) cfg).getEvolutionStrategyClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        evolution.init(((EvolvableEnsembleModelConfig) cfg).getEvolutionStrategyConfig(), this);

        maxInputs = cfg.getMaxInputsNumber();
        maxLearningVectors = cfg.getMaxLearningVectors();
        targetVariable = cfg.getTargetVariable();
        name = cfg.getName();
        learning_vectors = 0;

    }

    @Override
    protected void addBaseModel(int position, CfgTemplate config) {
        EvolvableModel me = new EvolvableModel();
        ModelConfig cfg = (ModelConfig) config;
        //cfg.setMaxLearningVectors(this.maxLearningVectors);
        me.init(config);
        addModel(position, me);
    }

    @Override
    public void setInputsNumber(int inputsNumber) {
        super.setInputsNumber(inputsNumber);
        Random rnd = new Random();
        for (EvolvableModel m : (ArrayList<EvolvableModel>) ensembleModels) {
            //m.setInputsNumber(inputsNumber); called in setDna() function
            int limit = rnd.nextInt(inputsNumber) + 1;
            if (maxInputs > 0) limit = maxInputs;
            Genome gg = new Genome(inputsNumber, limit);
            gg.initializeRandomly();
            m.setDna(gg);
        }
    }


    public <T extends ObjectEvolvable> void computeFitness(ArrayList<T> models) {
        for (ObjectEvolvable model : models) {
            if (model.getFitness() <= 0) {   // skip surving models (elite, etc)
                ModelLearnable m = (ModelLearnable) model;
                rndGenerator.resetRandom(); //clears the memory of generated learning vectors
                for (int i = 0; i < learnVectNum; i++) {
                    int rnd = rndGenerator.getRandomLearningVector();
                    if (i < m.getMaxLearningVectors()) m.storeLearningVector(this.inputVect[rnd], this.target[rnd]);

                }    // stores training data into model memory structures
                m.learn();

                double err = 0;
                rndGenerator.resetRandom();
                int num = validVectNum > 500 ? 500 : validVectNum;
                for (int j = 0; j < num; j++) {
                    int index = rndGenerator.getRandomTestingVector();
                    double out = ((ModelEvolvable) model).getOutput(inputVect[index]);
                    err += (out - target[index]) * (out - target[index]);
                }
                model.setFitness(1 / (1 + err));
            }
        }
    }

    public <T extends ObjectEvolvable> T produceOffspring(Dna dna) {
        EvolvableModel me = new EvolvableModel();
        Random rnd = new Random();
        me.init(baseModelsCfg.get(rnd.nextInt(baseModelsCfg.size())));
        me.setDna(dna);
        logger.trace("New model, class:" + me.getClass().getName() + ", configuration:" + me.getConfigClass());
        logger.trace("Model genome (input connections):" + dna.toString());
        return (T) me;

    }

    public <T extends ObjectEvolvable> T produceRandomOffspring() {
        Random rnd = new Random();
        int limit = rnd.nextInt(inputsNumber) + 1;
        if (maxInputs > 0) limit = maxInputs;
        Genome gg = new Genome(inputsNumber, limit);
        gg.initializeRandomly();
        EvolvableModel me = new EvolvableModel();
        me.init(baseModelsCfg.get(rnd.nextInt(baseModelsCfg.size())));
        me.setDna(gg);
        return (T) me;
    }

    @Override
    public double getDistance(ObjectEvolvable model1, ObjectEvolvable model2) {
        //genotypic
        double dist = 0;
        if (model1.equals(model2)) return dist;
        if (genomeDistEnabled)
            dist += model1.getDna().distance(model2.getDna());

        // logger.trace("Gdistance="+gdistance);
        //phenotypic
        if (corrDistEnabled || outputDistEnabled)
            dist += 1000 * computeDistanceOfOuputs((ModelEvolvable) model1, (ModelEvolvable) model2);
        //todo normalize distances
        return dist;
    }

    /**
     * Computes difference in errors for two models on validation data
     *
     * @param model1 model 1
     * @param model2 model 2
     * @return mean squared difference of errors on validation data
     */
    private double computeDistanceOfOuputs(ModelEvolvable model1, ModelEvolvable model2) {
        double dist = 0, v;
        rndGenerator.resetRandom();
        int num = validVectNum > 500 ? 500 : validVectNum;
        for (int j = 0; j < num; j++) {
            int index = rndGenerator.getRandomTestingVector();
            double out1 = model1.getOutput(inputVect[index]);
            double out2 = model2.getOutput(inputVect[index]);
            if (corrDistEnabled) {
                v = (out1 - target[index]) * (out2 - target[index]);
                if (v < 0) dist += -v;
            }
            if (outputDistEnabled) {
                v = (out1 - out2) * (out1 - out2);
                dist += v;
            }
        }
        dist /= num;
        return dist;
    }

    public void learn() {

        learnVectNum = learning_vectors * learnValidRatio / 100;
        validVectNum = learning_vectors - learnVectNum;

        prepareLearningAndValidationData();
        logger.info("Data prepared");
        //  createBaseModels();  created already in init()
        logger.info("Initial population generated");
        logger.info("Evolution starts");
        logger.info("Number of generations: " + generations);
        computeFitness((ArrayList<EvolvableModel>) ensembleModels);
        for (int generation = 0; generation < generations; generation++) {
            logger.debug("Generation " + generation);
            ensembleModels = evolution.newGeneration((ArrayList<EvolvableModel>) ensembleModels);
        }
        ensembleModels = evolution.getFinalPopulation((ArrayList<EvolvableModel>) ensembleModels);
        logger.info("Evolution ends, selecting survivals");
    }


    protected void prepareLearningAndValidationData() {
        rndGenerator = new MyRandom(learning_vectors);
        rndGenerator.generateLearningAndTestingSet(validVectNum);
    }

    /**
     * Returns simple average of surviving models
     *
     * @param input_vector Specify inputs to the model
     * @return simple ensemble of evolved models
     */
    public double getOutput(double[] input_vector) {
        double out = 0;
        for (Model model : ensembleModels) {
            out += model.getOutput(input_vector);
        }
        out /= ensembleModels.size();
        return out;
    }

    public String toEquation(String[] inputEquation) {
        if (ensembleModels.size() == 0) return null;
        if (ensembleModels.size() == 1)
            return ensembleModels.get(0).toEquation(inputEquation);

        String out = "(";
        for (Model model : ensembleModels) {
            out += "(" + model.toEquation(inputEquation) + ")+";
        }
        out = out.substring(0, out.length() - 1); // remove last "+" character
        out += ")/" + ensembleModels.size();
        return out;
    }

    public Class getConfigClass() {
        return EvolvableEnsembleModelConfig.class;
    }

    public void relearn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void learn(int modelIndex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}