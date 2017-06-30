package game.evolution.treeEvolution;

import game.models.ensemble.WeightedRandom;
import game.utils.MyRandom;
import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Random;

/**
 * Coevolution for arrays, namely binary array (= classic genome evolution).
 */
public class ArrayCoEvolution implements CoEvolution {

    protected int currentGeneration = 0;
    protected Random rnd;
    protected WeightedRandom weightedRnd;
    protected Logger log;

    protected Hashtable<String, Double> fitnessDB;
    protected Individual[] generation;
    protected Individual bestIndividual;
    protected double bestFitness;


    /**
     * Class representing individual. Individual has gene (boolean array) and a fitness.
     */
    private class Individual implements Cloneable {
        private boolean[] gene;
        private String key;

        public Individual clone() {
            Individual newObject;
            try {
                newObject = (Individual) super.clone();
                System.arraycopy(gene, 0, newObject.gene, 0, gene.length);
                newObject.key = key;
                return newObject;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        public Individual(boolean[] gene) {
            this.gene = new boolean[gene.length];
            System.arraycopy(gene, 0, this.gene, 0, gene.length);
            key = booleanArrayToString(gene);
        }

        public Individual(boolean[] gene, String key) {
            this.gene = new boolean[gene.length];
            System.arraycopy(gene, 0, this.gene, 0, gene.length);
            this.key = key;
        }

        private String booleanArrayToString(boolean[] array) {
            String output = "";
            for (int i = 0; i < array.length; i++) {
                if (array[i]) output += "1";
                else output += "0";
            }
            return output;
        }

        public void setGene(boolean[] gene) {
            this.gene = gene;
            key = booleanArrayToString(gene);
        }

        public boolean[] getGene() {
            boolean[] newGene = new boolean[gene.length];
            System.arraycopy(gene, 0, newGene, 0, gene.length);
            return newGene;
        }

        public String getKey() {
            return key;
        }
    }


    public ArrayCoEvolution(boolean[][] initGenTemplates, int generationSize) {
        rnd = new Random();
        fitnessDB = new Hashtable<String, Double>(50);
        log = Logger.getLogger(this.getClass());

        generation = new Individual[generationSize];

        createInitGeneration(initGenTemplates);
        bestFitness = Double.NEGATIVE_INFINITY;
        bestIndividual = generation[0].clone();

        weightedRnd = new WeightedRandom();

        double[] initWeights = new double[generation.length];
        for (int i = 0; i < initWeights.length; i++) initWeights[i] = 1;
        weightedRnd.recomputeWeights(initWeights);
    }

    private double[] getFitnessArray(Individual[] generation) {
        double[] fitness = new double[generation.length];
        double averageFitness = 0;
        int count = 0;
        for (int i = 0; i < generation.length; i++) {
            if (fitnessDB.containsKey(generation[i].getKey())) {
                fitness[i] = fitnessDB.get(generation[i].getKey());
                fitness[i] = fitness[i] * fitness[i];
                averageFitness += fitness[i];
                count++;
            } else {
                fitness[i] = Double.NEGATIVE_INFINITY;
            }
        }

        if (count != 0) averageFitness = averageFitness / count;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] == Double.NEGATIVE_INFINITY) fitness[i] = averageFitness;
        }
        return fitness;
    }

    /**
     * Creates initial generation based on initGenTemplates. These templates are first copied, then randomly selected and
     * altered via mutation if all templates all already copied.
     *
     * @param initGenTemplates Templates for initial generation.
     */
    protected void createInitGeneration(boolean[][] initGenTemplates) {
        int index;
        MyRandom rndWithoutRep = new MyRandom(initGenTemplates.length);
        for (int i = 0; i < generation.length; i++) {
            index = rndWithoutRep.nextInt(initGenTemplates.length);
            if (i < initGenTemplates.length) {
                generation[i] = new Individual(initGenTemplates[index]);
            } else { //mutate templates if there already are in the generation
                generation[i] = mutate(new Individual(initGenTemplates[index]));
            }
        }
    }

    @Override
    public Object selectIndividual() {
        return generation[weightedRnd.randomWeightedNumber()].getGene();
    }

    @Override
    public void saveFitness(Object individual, double fitness) {
        String key = booleanArrayToString((boolean[]) individual);
        if (fitnessDB.containsKey(key)) {
            fitnessDB.put(key, Math.max(fitness, fitnessDB.get(key)));
        } else {
            fitnessDB.put(key, fitness);
        }

        if (fitness > bestFitness) {
            bestFitness = fitness;
            bestIndividual = new Individual((boolean[]) individual, key);
        }
    }

    @Override
    public void nextIteration() {
        log.info("coevolution generation " + currentGeneration);

        Individual[] survivors = survivalCheck(generation.length / 2);
        //generate 50% of individuals from survivors
        generation = fillGeneration(survivors, generation.length);
        currentGeneration++;
        weightedRnd.recomputeWeights(getFitnessArray(generation));
        log.debug("best solution: " + bestFitness + " " + bestIndividual.key);
    }

    /**
     * Fills the generation from parents size to generationSize. All parents are copied into new generation. Rest is created
     * by mutation from parents (select random parent by selection method, clone it and modify it by mutation).
     *
     * @param parents        Generation of parents.
     * @param generationSize Size of final generation.
     * @return Returns complete generation.
     */
    private Individual[] fillGeneration(Individual[] parents, int generationSize) {
        Individual[] newGeneration = new Individual[generationSize];
        for (int i = 0; i < parents.length; i++) {
            newGeneration[i] = parents[i];
        }

        weightedRnd.recomputeWeights(getFitnessArray(parents));
        for (int i = parents.length; i < newGeneration.length; i++) {
            newGeneration[i] = mutate(selection(parents).clone());
        }

        return newGeneration;
    }

    private Individual selection(Individual[] parents) {
        return parents[weightedRnd.randomWeightedNumber()];
    }

    private Individual mutate(Individual parent) {
        boolean[] gene = parent.getGene();
        //todo: add bonus or penalty based on fitness
        int numberOfChanges = getNumberOfChanges(gene.length);

        int rndNum;
        for (int j = 0; j < numberOfChanges; j++) {
            rndNum = rnd.nextInt(gene.length);
            gene[rndNum] = !gene[rndNum];
        }
        parent.setGene(gene);
        return parent;
    }

    private int getNumberOfChanges(int maxValue) {
        int numberOfChanges;
        do {
            numberOfChanges = (int) Math.round(rnd.nextGaussian() * Math.sqrt(maxValue));
            if (numberOfChanges < 0) numberOfChanges = Math.abs(numberOfChanges);
        } while (numberOfChanges > maxValue || numberOfChanges == 0);
        return numberOfChanges;
    }


    private Individual[] survivalCheck(int numSurvivors) {
        Individual[] survivors = new Individual[numSurvivors];

        weightedRnd.recomputeWeights(getFitnessArray(generation));
        //select 50% of the individuals randomly based on fitness
        int rndNum;
        survivors[0] = bestIndividual.clone();
        for (int i = 1; i < numSurvivors; i++) {
            rndNum = weightedRnd.randomWeightedNumber();
            //clone due to the fact that individual may be chosen more than once
            survivors[i] = generation[rndNum].clone();
        }
        return survivors;
    }

    public String booleanArrayToString(boolean[] array) {
        String output = "";
        for (int i = 0; i < array.length; i++) {
            if (array[i]) output += "1";
            else output += "0";
        }
        return output;
    }


}