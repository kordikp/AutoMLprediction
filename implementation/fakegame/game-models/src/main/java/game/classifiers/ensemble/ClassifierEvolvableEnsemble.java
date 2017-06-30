package game.classifiers.ensemble;

import configuration.CfgTemplate;
import game.classifiers.Classifier;
import game.classifiers.evolution.ClassifierEvolvable;
import game.classifiers.evolution.EvolvableClassifier;
import game.evolution.Dna;
import game.evolution.EvolutionContext;
import game.evolution.EvolutionStrategy;
import game.evolution.Genome;
import game.evolution.ObjectEvolvable;
import game.utils.MyRandom;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.EvolvableEnsembleClassifierConfig;

/**
 * Model ensemble evolver for population of models represented by Genome
 */
public class ClassifierEvolvableEnsemble extends ClassifierEnsembleBase implements EvolutionContext {
    static Logger logger = Logger.getLogger(ClassifierEvolvableEnsemble.class);
    protected MyRandom rndGenerator;
    protected int generations;
    EvolutionStrategy evolution;
    int actualLayer;
    int learnValidRatio;
    int learnVectNum;  //learning data indexes
    int validVectNum;  //validation data indexes

    @Override
    public void init(ClassifierConfig cfg) {
        super.init(cfg);
        actualLayer = ((EvolvableEnsembleClassifierConfig) cfg).getMaxInputsNumber();
        if (actualLayer == -1) actualLayer = 0;
        learnValidRatio = ((EvolvableEnsembleClassifierConfig) cfg).getLearnValidRatio();
        generations = ((EvolvableEnsembleClassifierConfig) cfg).getGenerations();
        this.numClassifiers = ((EvolvableEnsembleClassifierConfig) cfg).getClassifiersNumber();
        try {
            evolution = (EvolutionStrategy) ((EvolvableEnsembleClassifierConfig) cfg).getEvolutionStrategyClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        evolution.init(((EvolvableEnsembleClassifierConfig) cfg).getEvolutionStrategyConfig(), this);

        //inputs = cfg.getMaxInputsNumber();
        //     inputs = GlobalData.getInstance().getINumber();
        //     outputs = GlobalData.getInstance().getONumber();

        maxLearningVectors = cfg.getMaxLearningVectors();
//        inputVect = new double[maxLearningVectors][inputs];
//        target = new double[maxLearningVectors][outputs];
        name = cfg.getName();
        learning_vectors = 0;

        //    createBaseModels();
    }

    @Override
    protected void addBaseClassifier(int position, CfgTemplate config) {
        EvolvableClassifier me = new EvolvableClassifier();
        ClassifierConfig cfg = (ClassifierConfig) config;
        cfg.setMaxLearningVectors(this.maxLearningVectors);
        me.init(config);
        ((ArrayList<EvolvableClassifier>) ensClassifiers).add(position, me);
    }

    @Override
    public void setInputsNumber(int inputsNumber) {
        super.setInputsNumber(inputsNumber);
        Random rnd = new Random();
        for (EvolvableClassifier c : (ArrayList<EvolvableClassifier>) ensClassifiers) {
            int limit = rnd.nextInt(inputsNumber) + 1;
            if (actualLayer > 0) limit = actualLayer;
            Genome gg = new Genome(inputsNumber, limit);
            gg.initializeRandomly();
            c.setDna(gg);
        }
    }

    public void storeLearningVector(double[] input, double[] outputs) {
        super.storeLearningVector(input, outputs);
    }

    public <T extends ObjectEvolvable> void computeFitness(ArrayList<T> classifiers) {
        for (ObjectEvolvable classifier : classifiers) {
            if (classifier.getFitness() <= 0) {   // skip surving models (elite, etc)
                Classifier c = (Classifier) classifier;
                rndGenerator.resetRandom(); //clears the memory of generated learning vectors
                for (int i = 0; i < learnVectNum; i++) {
                    int rnd = rndGenerator.getRandomLearningVector();
                    if (i < c.getMaxLearningVectors()) c.storeLearningVector(this.inputVect[rnd], this.target[rnd]);

                }    // stores training data into model memory structures
                c.learn();

                double err = 0;
                rndGenerator.resetRandom();
                for (int j = 0; j < validVectNum; j++) {
                    int index = rndGenerator.getRandomTestingVector();
                    double[] out = c.getOutputProbabilities(inputVect[index]);
                    for (int i = 0; i < out.length; i++)
                        err += (out[i] - target[index][i]) * (out[i] - target[index][i]);
                }
                classifier.setFitness(1.0 / (1 + err));
                logger.trace("Classifier:" + classifier.getDna().toString() + ", fitness:" + classifier.getFitness());

            }
        }
    }

    public <T extends ObjectEvolvable> T produceOffspring(Dna dna) {
        EvolvableClassifier me = new EvolvableClassifier();
        Random rnd = new Random();
        me.init(baseClassifiersCfgs.get(rnd.nextInt(baseClassifiersCfgs.size())));
        me.setDna(dna);
        logger.trace("New Classifier, name:" + me.getClass().getName() + ", configuration:" + me.getConfigClass());
        logger.trace("Classifier genome (input connections):" + dna.toString());
        return (T) me;

    }

    public <T extends ObjectEvolvable> T produceRandomOffspring() {
        Random rnd = new Random();
        int limit = rnd.nextInt(inputs) + 1;
        if (actualLayer > 0) limit = actualLayer;
        Genome gg = new Genome(inputs, limit);
        gg.initializeRandomly();
        EvolvableClassifier me = new EvolvableClassifier();
        me.init(baseClassifiersCfgs.get(rnd.nextInt(baseClassifiersCfgs.size())));
        me.setDna(gg);
        return (T) me;
    }

    @Override
    public double getDistance(ObjectEvolvable classifier1, ObjectEvolvable classifier2) {
        //genotypic
        double gdistance = classifier1.getDna().distance(classifier2.getDna());
        //todo normalize distances
        // logger.trace("Gdistance="+gdistance);
        //phenotypic
        double pdistance = computeMeanSquaredDistanceOfErrors((ClassifierEvolvable) classifier1, (ClassifierEvolvable) classifier2);
        //  logger.trace("Pdistance="+pdistance*100);
        return gdistance + pdistance * 100;
    }

    /**
     * Computes difference in errors for two models on validation data
     *
     * @param model1 model 1
     * @param model2 model 2
     * @return mean squared difference of errors on validation data
     */
    private double computeMeanSquaredDistanceOfErrors(ClassifierEvolvable model1, ClassifierEvolvable model2) {
        double err1, err2, err = 0;
        rndGenerator.resetRandom();
        for (int j = 0; j < validVectNum; j++) {
            int index = rndGenerator.getRandomTestingVector();
            double[] out1 = model1.getOutputProbabilities(inputVect[index]);
            double[] out2 = model2.getOutputProbabilities(inputVect[index]);
            for (int i = 0; i < out1.length; i++) {
                err1 = (out1[i] - target[index][i]) * (out1[i] - target[index][i]);
                err2 = (out2[i] - target[index][i]) * (out2[i] - target[index][i]);
                err += (err1 - err2) * (err1 - err2);
            }
        }
        err /= validVectNum;
        return err;
    }

    public void learn() {

        learnVectNum = learning_vectors * learnValidRatio / 100;
        validVectNum = learning_vectors - learnVectNum;

        prepareLearningAndValidationData();
        logger.info("Data prepared");
        //createBaseClassifiers();
        logger.info("Initial population generated");

        logger.info("Evolution starts");
        logger.info("Number of generations: " + generations);
        computeFitness((ArrayList<EvolvableClassifier>) ensClassifiers);
        for (int generation = 0; generation < generations; generation++) {
            logger.debug("Generation " + generation);
            ensClassifiers = evolution.newGeneration((ArrayList<EvolvableClassifier>) ensClassifiers);
        }
        ensClassifiers = evolution.getFinalPopulation((ArrayList<EvolvableClassifier>) ensClassifiers);
        logger.info("Evolution ends, selecting survivals");
        learned = true;
    }


    protected void prepareLearningAndValidationData() {
        rndGenerator = new MyRandom(learning_vectors);
        rndGenerator.generateLearningAndTestingSet(validVectNum);
    }


    public String toEquation(String[] inputEquation) {
        if (ensClassifiers.size() == 0) return null;
        if (ensClassifiers.size() == 1)
            return ensClassifiers.get(0).toEquation(inputEquation);

        String out = "(";
        for (Classifier c : ensClassifiers) {
            out += "(" + c.toEquation(inputEquation) + ")+";
        }
        out = out.substring(0, out.length() - 1); // remove last "+" character
        out += ")/" + ensClassifiers.size();
        return out;
    }

    public String[] getEquations(String[] inputEquation) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getConfigClass() {
        return EvolvableEnsembleClassifierConfig.class;
    }

    public void relearn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void learn(int modelIndex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        double[] out = new double[outputs];
        for (int i = 0; i < outputs; i++) {
            out[i] = 0;
        }
        double[] r;
        for (Classifier clas : ensClassifiers) {
            r = clas.getOutputProbabilities(input_vector);
            for (int i = 0; i < outputs; i++) {
                out[i] += r[i];
            }
        }
        double sum = 0;
        for (int i = 0; i < outputs; i++) {
            sum += out[i];
        }
        for (int i = 0; i < outputs; i++) {
            out[i] /= sum;
        }              // normalize probability
        return out;
    }
}