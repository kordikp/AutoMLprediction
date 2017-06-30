package game.evolution;

import game.evolution.Genome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.Random;

import configuration.evolution.AntEvolutionStrategyConfig;
import org.apache.log4j.Logger;

/**
 * Ant metaphor for creating ensembles of models, each model is connected to a subset of inputs.
 * Can be used for DNA consisting of Integer genes only!
 * <p/>
 * contact: oleg.kovarik@gmail.com
 */
public class AntEvolutionStrategy extends EvolutionStrategyBase implements EvolutionStrategy {
    static Logger logger = Logger.getLogger(AntEvolutionStrategy.class);

    double pheromone[][];       // pheromones for 0 and 1 bits

    // parameters (values stored in configuration package):
    double pheromoneInit;
    double pheromoneMin;
    double evaporation;
    double intensification;
    Random random;
    int randomSeed;
    private int inputsNumber;
    private int modelsNumber;

    /**
     * Implements one epoch of ant optimization
     */
    public <T extends ObjectEvolvable> ArrayList<T> newGeneration(ArrayList<T> oldPopulation) {
        if ((pheromone == null) || (oldPopulation.get(0).getDna().genes() != inputsNumber)) {
            modelsNumber = oldPopulation.size();
            inputsNumber = oldPopulation.get(0).getDna().genes();
            initPheromone();
        }
        ArrayList<T> newModels = new ArrayList<T>(modelsNumber);
        Genome h;

        for (int i = 0; i < modelsNumber; i++) {
            logger.trace("creating new genome[" + i + "]");
            h = generateSolution();
            newModels.add(i, (T) evolved.produceOffspring(h));
        }

        logger.debug("training offsprings, computing error");
        evolved.computeFitness(newModels);

        //sort models according to fitness
        Collections.sort(newModels);


        logger.debug("Replacing old population by new one");
        layPheromone(newModels);
        evaporatePheromone();
        return newModels;
    }

    private void initPheromone() {
        //System.out.println("init pheromone");
        pheromone = new double[2][inputsNumber];

        for (int i = 0; i < inputsNumber; i++) {
            pheromone[0][i] = pheromoneInit;
            pheromone[1][i] = pheromoneInit;
        }
    }

    private void evaporatePheromone() {
        for (int i = 0; i < inputsNumber; i++) {
            pheromone[0][i] -= evaporation;
            pheromone[1][i] -= evaporation;
        }

        for (int i = 0; i < inputsNumber; i++) {
            pheromone[0][i] = Math.max(pheromone[0][i], pheromoneMin);
            pheromone[1][i] = Math.max(pheromone[1][i], pheromoneMin);
        }
    }

    private <T extends ObjectEvolvable> void layPheromone(ArrayList<T> newModels) {
        Dna g = newModels.get(0).getDna();
        int gene = (Integer) g.getGene(0);
        for (int i = 0; i < inputsNumber; i++) {
            gene = (Integer) g.getGene(i);
            pheromone[gene][i] += intensification;
        }
        //printPheromoneProb();
    }

    private Genome generateSolution() {
        Genome solution = new Genome(inputsNumber, random.nextInt(inputsNumber - 1) + 1);
        int genes[] = new int[inputsNumber];
        double sum, rnd;

        for (int i = 0; i < inputsNumber; i++) {
            sum = pheromone[0][i] + pheromone[1][i];
            rnd = random.nextDouble() * sum;
            if (rnd > pheromone[0][i]) genes[i] = 1;
            else genes[i] = 0;
        }

        for (int i = 0; i < inputsNumber; i++) {
            solution.setGene(i, genes[i]);
        }

        return solution;
    }

    public void init(Object cfgBean, EvolutionContext context) {
        super.init(cfgBean, context);
        evaporation = ((AntEvolutionStrategyConfig) cfgBean).getEvaporation();
        this.intensification = ((AntEvolutionStrategyConfig) cfgBean).getIntensification();
        this.pheromoneInit = ((AntEvolutionStrategyConfig) cfgBean).getPheromoneInit();
        this.pheromoneMin = ((AntEvolutionStrategyConfig) cfgBean).getPheromoneMin();
        random = new Random(((AntEvolutionStrategyConfig) cfgBean).getRandomSeed());
    }


    private void printGenes(Genome gene) {
        System.out.print("Genes: ");
        for (int i = 0; i < inputsNumber; i++) {
            System.out.print(gene.getGene(i) + " ");
        }
        System.out.println("");
    }

    public void printPheromone() {
        for (int i = 0; i < inputsNumber; i++) {
            System.out.print(" " + pheromone[0][i]);
        }
        System.out.println("");
        for (int i = 0; i < inputsNumber; i++) {
            System.out.print(" " + pheromone[1][i]);
        }
        System.out.println("");
    }

    public void printPheromoneProb() {
        for (int i = 0; i < inputsNumber; i++) {
            System.out.print(" " + (pheromone[1][i] / pheromone[0][i]));
        }
        System.out.println("");
    }
}
