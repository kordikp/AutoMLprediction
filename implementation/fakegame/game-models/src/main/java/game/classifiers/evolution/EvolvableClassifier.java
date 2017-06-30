package game.classifiers.evolution;

import configuration.CfgTemplate;
import game.classifiers.Classifier;
import game.classifiers.Classifiers;
import game.evolution.Genome;
import game.evolution.ObjectEvolvable;
import game.evolution.Dna;
import configuration.classifiers.ClassifierConfig;
import org.apache.log4j.Logger;

/**
 * Wrapper Class providing genome and fitness for Classifier evolution
 *
 * @author kordikp
 */
public class EvolvableClassifier implements ClassifierEvolvable, Classifier {
    static Logger logger = Logger.getLogger(EvolvableClassifier.class);

    protected Classifier classifier;
    protected double fitness;
    protected Genome genome;

    public void init(CfgTemplate classifierConfig) {
        ClassifierConfig cfg = (ClassifierConfig) classifierConfig;

        //modify config by genome
        //todo init(classifierConfig);
        Classifier m = Classifiers.newInstancebyClassName(classifierConfig.getClassRef());
        m.init(cfg);
        classifier = m;
    }

    public double[] adaptInputVector(double[] input_vector) {

        if (input_vector.length != genome.genes()) {
            logger.error("Input vector to classifier doesnt match size of genome !!!");
            return null;
        }
        int length = genome.countInputs();
        double[] new_vect = new double[length];
        int idx = 0;
        for (int i = 0; i < length; i++) {
            while ((Integer) genome.getGene(idx) == 0) idx++;
            new_vect[i] = input_vector[idx++];
        }
        return new_vect;
    }

    public String[] adaptInputEquation(String[] input_equation) {
        if (input_equation.length != genome.genes()) {
            logger.error("Input equation to classifier doesnt match size of genome !!!");
            return null;
        }
        int length = genome.countInputs();
        String[] new_vect = new String[length];
        int idx = 0;
        for (int i = 0; i < length; i++) {
            while ((Integer) genome.getGene(idx) == 0) idx++;
            new_vect[i] = input_equation[idx++];
        }
        return new_vect;
    }

    public ClassifierConfig getConfig() {
        return classifier.getConfig();
    }

    public Genome getDna() {
        return genome;
    }

    public void setDna(Dna dna) {
        this.genome = (Genome) dna;
        classifier.setInputsNumber(genome.countInputs());
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int compareTo(ObjectEvolvable m) {
        return (this.fitness - m.getFitness()) > 0 ? -1 : 1;

    }

    public String getName() {
        return classifier.getName();
    }

    public void setName(String name) {
        classifier.setName(name);
    }

    public int getOutput(double[] input_vector) {
        return classifier.getOutput(adaptInputVector(input_vector));
    }

    public String toEquation(String[] inputEquation) {
        return classifier.toEquation(adaptInputEquation(inputEquation));
    }

    @Override
    public String[] getEquations(String[] inputEquation) {
        return classifier.getEquations(adaptInputEquation(inputEquation));
    }

    public Class getConfigClass() {
        return classifier.getConfigClass();
    }

    public void init(ClassifierConfig cfg) {
        classifier.init(cfg);
    }

    public void learn() {
        classifier.learn();
    }

    @Override
    public void relearn() {
        classifier.relearn();
    }

    @Override
    public double[] getOutputProbabilities(double[] input_vector) {
        return classifier.getOutputProbabilities(adaptInputVector(input_vector));
    }

    public int getMaxLearningVectors() {
        return classifier.getMaxLearningVectors();
    }

    public void setMaxLearningVectors(int maxVectors) {
        classifier.setMaxLearningVectors(maxVectors);
    }

    public void storeLearningVector(double[] input, double[] outputs) {
        classifier.storeLearningVector(adaptInputVector(input), outputs);
    }

    public void deleteLearningVectors() {
        classifier.deleteLearningVectors();
    }

    public boolean isLearned() {
        return classifier.isLearned();
    }

    public void resetLearningData() {
        classifier.resetLearningData();
    }

    public void setInputsNumber(int inputs) {
        classifier.setInputsNumber(inputs);
    }

    public void setOutputsNumber(int outputs) {
        classifier.setOutputsNumber(outputs);
    }

    public int getInputsNumber() {
        return classifier.getInputsNumber();
    }

    public int getOutputsNumber() {
        return classifier.getOutputsNumber();
    }

    public double[][] getLearningInputVectors() {
        return classifier.getLearningInputVectors();
    }

    public double[][] getLearningOutputVectors() {
        return classifier.getLearningOutputVectors();
    }
}
