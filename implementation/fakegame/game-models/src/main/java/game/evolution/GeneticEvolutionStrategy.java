package game.evolution;


import configuration.evolution.GeneticEvolutionStrategyConfig;

import java.util.*;

import game.utils.MyRandom;
import org.apache.log4j.Logger;

/**
 * Evolves population of models , each model is connected to a subset of inputs
 */
public class GeneticEvolutionStrategy extends EvolutionStrategyBase implements EvolutionStrategy {
    static Logger logger = Logger.getLogger(GeneticEvolutionStrategy.class);
    protected double mutationRate;


    /**
     * Implements one epoch of genetic algorithm
     */
    public <T extends ObjectEvolvable> ArrayList<T> newGeneration(ArrayList<T> oldPopulation) {
        int modelsNumber = oldPopulation.size();
        int[] parent = new int[modelsNumber];
        ArrayList<T> newPopulation = new ArrayList<T>();
        Dna[] h;
        logger.debug("Computing error of models on validation data");
        //todo ensure fitness computed for the initial population

        MyRandom rndModel = new MyRandom(modelsNumber); // parents randomly in pairs
        for (int i = 0; i < modelsNumber; i++) {
            parent[i] = rndModel.getRandom(modelsNumber);
        }
        logger.debug("Selecting parents and producing offsprings");
        for (int i = 0; i < modelsNumber - 1; i += 2) {
            logger.trace("Making love: indexes [" + parent[i] + ", " + parent[i + 1] + "]");
            T p1 = oldPopulation.get(parent[i]);
            T p2 = oldPopulation.get(parent[i + 1]);
            h = p1.getDna().cross(p2.getDna());
            h[0].mutate(mutationRate);
            h[1].mutate(mutationRate);  // produce offsprings and store it to the new population
            newPopulation.add(i, (T) evolved.produceOffspring(h[0]));
            newPopulation.add(i + 1, (T) evolved.produceOffspring(h[1]));
        }
        if (modelsNumber % 2 == 1) { //odd number of models in population - mutate last parent
            T p = oldPopulation.get(parent[modelsNumber - 1]);
            Dna g = p.getDna();
            g.mutate(mutationRate);
            newPopulation.add(modelsNumber - 1, (T) evolved.produceOffspring(g));
        }

        logger.debug("training offsprings, computing error");

        evolved.computeFitness(newPopulation);

        produceNewGeneration(oldPopulation, newPopulation, parent);
        // if(elitism) elitism(oldPopulation, newPopulation);

        Collections.sort(newPopulation);

        logger.debug("Replacing old population by new one");
        //oldPopulation = newPopulation;
        return newPopulation;
    }


    /**
     * Copies individuals from the old population to new population according to their fitness
     *
     * @param oldPopulation population in actual generation
     * @param newPopulation next generation
     * @param parent        indexes of parents that were used to produce offsprings in the new population
     */
    protected <T extends ObjectEvolvable> void produceNewGeneration(ArrayList<T> oldPopulation, ArrayList<T> newPopulation, int[] parent) {
        for (int i = 0; i < oldPopulation.size(); i++) {
            if ((oldPopulation.get(parent[i])).getFitness() > newPopulation.get(i).getFitness()) {
                newPopulation.set(i, oldPopulation.get(parent[i]));
            } else logger.trace("Model " + parent[i] + " replaced by offspring " + i);
        } // no crowding - simple GA

    }

    public void init(Object cfgBean, EvolutionContext context) {
        super.init(cfgBean, context);
        mutationRate = ((GeneticEvolutionStrategyConfig) cfgBean).getMutationRate();
    }


    public boolean isFinished() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
