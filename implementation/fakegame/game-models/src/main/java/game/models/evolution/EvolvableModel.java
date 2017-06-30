package game.models.evolution;


import configuration.CfgTemplate;
import game.evolution.Dna;
import game.evolution.Genome;
import game.evolution.ObjectEvolvable;
import game.models.ModelLearnable;
import game.models.Models;

import org.apache.log4j.Logger;

import configuration.models.ModelConfig;

/**
 * Wrapper Class providing genome and fitness for model evolution
 *
 * @author kordikp
 */
public class EvolvableModel implements ModelLearnable, ModelEvolvable {
    static Logger logger = Logger.getLogger(EvolvableModel.class);

    protected ModelLearnable model;
    protected double fitness;
    protected Genome genome;

    public void init(CfgTemplate modelConfig) {
        ModelConfig cfg = (ModelConfig) modelConfig;
        ModelLearnable m = Models.newInstancebyClassName(modelConfig.getClassRef());
        m.setMaxLearningVectors(cfg.getMaxLearningVectors());
        m.init(cfg);
        model = m;
    }

    public double[] adaptInputVector(double[] input_vector) {
        if (input_vector.length != genome.genes()) {
            logger.error("Input vector to model doesnt match size of genome !!!");
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
            logger.error("Input equation to model doesnt match size of genome !!!");
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

    public ModelConfig getConfig() {
        ModelConfig cfg = null;
        try {
            cfg = (ModelConfig) getConfigClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public Dna getDna() {
        return genome;
    }

    public void setDna(Dna dna) {
        this.genome = (Genome) dna;
        int inputs = genome.countInputs();
        model.setInputsNumber(inputs);
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
        return model.getName();
    }

    public void setName(String name) {
        model.setName(name);
    }

    public String getTrainedBy() {
        return model.getTrainedBy();
    }

    public void setTrainedBy(String trainerName) {
        model.setTrainedBy(trainerName);
    }

    public double getOutput(double[] input_vector) {
        return model.getOutput(adaptInputVector(input_vector));
    }

    public int getTargetVariable() {
        return model.getTargetVariable();
    }

    public void setTargetVariable(int targetVariable) {
        model.setTargetVariable(targetVariable);
    }

    public String toEquation(String[] inputEquation) {
        return model.toEquation(adaptInputEquation(inputEquation));
    }

    public Class getConfigClass() {
        return model.getConfigClass();
    }

    public void init(ModelConfig cfg) {
        model.init(cfg);
    }

    public void learn() {
        model.learn();
    }

    public int getMaxLearningVectors() {
        return model.getMaxLearningVectors();
    }

    public void setMaxLearningVectors(int maxVectors) {
        model.setMaxLearningVectors(maxVectors);
    }

    public void storeLearningVector(double[] input, double output) {
        model.storeLearningVector(adaptInputVector(input), output);
    }

    public void deleteLearningVectors() {
        model.deleteLearningVectors();
    }

    public boolean isLearned() {
        return model.isLearned();
    }

    public void resetLearningData() {
        model.resetLearningData();
    }

    public void setInputsNumber(int inputs) {
        model.setInputsNumber(inputs);
    }

    public int getInputsNumber() {
        return model.getInputsNumber();
    }

    @Deprecated
    public double getNormalizedOutput(double[] normalized_input_vector) {
        return model.getNormalizedOutput(normalized_input_vector);
    }

    public double[][] getLearningInputVectors() {
        return model.getLearningInputVectors();
    }

    public double[] getLearningOutputVectors() {
        return model.getLearningOutputVectors();
    }
}
