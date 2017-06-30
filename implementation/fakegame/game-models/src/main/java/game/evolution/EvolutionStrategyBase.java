package game.evolution;


import java.util.*;

import org.apache.log4j.Logger;
import configuration.evolution.BaseEvolutionStrategyConfig;

/**
 * Evolves population of models , each model is connected to a subset of inputs
 */
public abstract class EvolutionStrategyBase implements EvolutionStrategy {
    static Logger logger = Logger.getLogger(EvolutionStrategyBase.class);
    protected EvolutionContext evolved;
    //protected boolean elitism;
    //protected int elitists;
    protected boolean singleSolution;
    protected int maxSurvivals;
    double distWeight;

 /*   protected <T extends ObjectEvolvable> void elitism(Vector<T> oldPopulation, Vector<T> newPopulation) {
        //sort models according to fitness
        Collections.sort(oldPopulation);
        Collections.sort(newPopulation);
        int size = newPopulation.size();
        int start = size - elitists;
        if (start < 0) start = 0; //more elitists than population size
        int index = 0;
        for (int i = start; i < size; i++) {
            if (oldPopulation.get(index).getFitness() > newPopulation.get(i).getFitness())
                newPopulation.set(i, oldPopulation.get(index++));
        }
    }
   */

    /**
     * This function returns final population - removes all individuals except the elite one
     *
     * @param evolvedPopulation population in the last generation
     * @return surviving individuals
     */
    protected <T extends ObjectEvolvable> ArrayList<T> getSingleOptimum(ArrayList<T> evolvedPopulation) {
        ArrayList<T> survivingModels = new ArrayList<T>();
        survivingModels.add(evolvedPopulation.get(0));
        return survivingModels;
    }


    /**
     * This function returns best solution from each niche (based on genotypic distance)
     *
     * @param evolvedPopulation population in the last generation
     * @return surviving individuals
     */
    protected <T extends ObjectEvolvable> ArrayList<T> getMultipleOptima(ArrayList<T> evolvedPopulation) {
        ArrayList<T> survivingModels = this.getSingleOptimum(evolvedPopulation);
        T winner = null;
        double bestCR;
        T champion = survivingModels.get(0);

        for (int i = 1; i < maxSurvivals; i++) {
            winner = null;
            bestCR = Double.MAX_VALUE;
            int num = 0;
            for (T model : evolvedPopulation)
                if (!survivingModels.contains(model)) num++;

            double[] distance = new double[num];
            double[] error = new double[num];
            int idx = 0;
            for (T model : evolvedPopulation) {
                if (!survivingModels.contains(model)) {
                    distance[idx] = computeDistance(model, survivingModels);
                    error[idx++] = champion.getFitness() - model.getFitness();
                }
            }
            distance = game.trainers.palmath.MathUtils.getNormalized(distance);
            error = game.trainers.palmath.MathUtils.getNormalized(error);
            for (int j = 0; j < num; j++) error[j] = (1 - distWeight) * error[j] - distWeight * distance[j];
            int winIdx = 0;
            double winErr = Double.MAX_VALUE;
            for (int j = 0; j < num; j++)
                if (error[j] < winErr) {
                    winIdx = j;
                    winErr = error[j];   // find winning index
                }
            for (T model : evolvedPopulation) {
                if (!survivingModels.contains(model)) {
                    winIdx--;        // locate and add the winner
                    if (winIdx < 0) {
                        survivingModels.add(model);
                        break;
                    }
                }
            }

        }
        return survivingModels;
        /*
        if(distance>0) {
                   double aCR = champion.getFitness() - model.getFitness();
                   aCR = distance distWeight ;
                   if(aCR < bestCR) {
                       bestCR = aCR;
                       winner = model;
                   }

               }



        int i = 0;
        do {
            logger.trace("Dna:" + evolvedPopulation.get(i++).getDna());
        } while (i < evolvedPopulation.size());
        i = 0;
        while (i < evolvedPopulation.size()) {
            int j = i + 1;
            while (j < evolvedPopulation.size()) {

                if (evolvedPopulation.get(i).getDna().equals(evolvedPopulation.get(j).getDna())) {
                    logger.trace("Model deleted " + j + "(worse performance than a niche leader)");
                    evolvedPopulation.removeElementAt(j);
                } else j++;

            }
            i++;

        }
        while (evolvedPopulation.size() > maxSurvivals) {
            logger.trace("Model deleted " + maxSurvivals + "(max surviving treshold exceeded)");

            evolvedPopulation.removeElementAt(maxSurvivals);
        }

        return evolvedPopulation; */
    }

    /**
     * Computes distance of the individual (model) from already selected individuals
     *
     * @param model           The reference individual
     * @param survivingModels set of presecteded individuals
     * @param <T>             Models, Classifiers or any Object to be evolved
     * @return distance of model from surviving individuals
     */
    private <T extends ObjectEvolvable> double computeDistance(T model, ArrayList<T> survivingModels) {
        double distance = 1;
        if (survivingModels.contains(model)) return 0;
        for (T m : survivingModels) {
            distance *= evolved.getDistance(m, model);
        }
        return distance;
    }


    /**
     * This function returns final population
     *
     * @param evolvedPopulation population in the last generation
     * @return surviving individuals
     */
    public <T extends ObjectEvolvable> ArrayList<T> getFinalPopulation(ArrayList<T> evolvedPopulation) {
        if (singleSolution) return getSingleOptimum(evolvedPopulation);
        else return getMultipleOptima(evolvedPopulation);
    }

    public boolean isFinished() {
        return false;
    }

    public void init(Object cfgBean, EvolutionContext context) {
        //   elitism = ((BaseEvolutionStrategyConfig) cfgBean).isElitism();
        //   elitists = ((BaseEvolutionStrategyConfig) cfgBean).getElitists();
        singleSolution = ((BaseEvolutionStrategyConfig) cfgBean).isSingleSolution();
        maxSurvivals = ((BaseEvolutionStrategyConfig) cfgBean).getMaxSurvivals();
        distWeight = ((BaseEvolutionStrategyConfig) cfgBean).getDistWeight();

        evolved = context;
    }

}