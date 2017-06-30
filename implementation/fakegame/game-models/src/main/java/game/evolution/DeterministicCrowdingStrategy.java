package game.evolution;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * Deterministic Crowding preserves diversity in the population. It can find multiple diverse optima.
 */
public class DeterministicCrowdingStrategy extends GeneticEvolutionStrategy implements EvolutionStrategy {
    static Logger logger = Logger.getLogger(DeterministicCrowdingStrategy.class);

    @Override
    protected <T extends ObjectEvolvable> void produceNewGeneration(ArrayList<T> oldPopulation, ArrayList<T> newPopulation, int[] parent) {
        for (int i = 0; i < oldPopulation.size() - 1; i += 2) {
            double dist1 = evolved.getDistance(oldPopulation.get(parent[i]), newPopulation.get(i));
            dist1 += evolved.getDistance(oldPopulation.get(parent[i + 1]), newPopulation.get(i + 1));
            double dist2 = evolved.getDistance(oldPopulation.get(parent[i]), newPopulation.get(i + 1));
            dist2 += evolved.getDistance(oldPopulation.get(parent[i + 1]), newPopulation.get(i));

            if (dist1 > dist2) {
                if (oldPopulation.get(parent[i]).getFitness() < newPopulation.get(i + 1).getFitness()) {
                    logger.trace("Model " + parent[i] + " replaced by offspring " + (i + 1));
                } else newPopulation.set(i + 1, oldPopulation.get(parent[i]));

                if (oldPopulation.get(parent[i + 1]).getFitness() < newPopulation.get(i).getFitness()) {
                    logger.trace("Model " + parent[i + 1] + " replaced by offspring " + (i));
                } else newPopulation.set(i, oldPopulation.get(parent[i + 1]));

            } else {
                if (oldPopulation.get(parent[i]).getFitness() < newPopulation.get(i).getFitness()) {
                    logger.trace("Model " + parent[i] + " replaced by offspring " + i);
                } else newPopulation.set(i, oldPopulation.get(parent[i]));

                if (oldPopulation.get(parent[i + 1]).getFitness() < newPopulation.get(i + 1).getFitness()) {
                    logger.trace("Model " + parent[i + 1] + " replaced by offspring " + (i + 1));
                } else newPopulation.set(i + 1, oldPopulation.get(parent[i + 1]));

            }


        } // deterministic crowdingif(oldPopulation.size()%2==1) {
        int i = oldPopulation.size() - 1;
        if (oldPopulation.get(parent[i]).getFitness() < newPopulation.get(i).getFitness()) {
            logger.trace("Model " + parent[i] + " replaced by offspring " + i);
        } else newPopulation.set(i, oldPopulation.get(parent[i]));

    }

}