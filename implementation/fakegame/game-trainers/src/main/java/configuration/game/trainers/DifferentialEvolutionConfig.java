/*
 * DifferentialEvolutionConfig.java
 * - represents configuration GUI for DifferentialEvolutionTrainer algorithm
 * Created on 22. June 2006, 9:19
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

/**
 * @author Miroslav Janosik (janosm2@fel.cvut.cz)
 */
public class DifferentialEvolutionConfig extends AbstractCfgBean {
    /*
    * DE parametrs
    */
    // populationSize ... Number of Parents (size of population)
    int populationSize = 10; // recommanded <4, 100>; optimum 10+
    // mutationRate ... mutation constant
    double mutationRate = 0.6; // recommanded <0,2>; optimum <0.3, 0.9>
    // crossoverRate ... Crossover Rating
    double crossoverRate = 0.85; // recommanded <0,1>; optimum <0.8, 0.9>
    // maxGenerations ... maximal generations
    int maxGenerations = 300; // depends on user


    // defines howmany generations should alg. continue if the bestError is the same
    int maxGenerationsWithoutChange = 20;
    // defines borders for parametrs value in first generation
    public static double INITMAX = 10;

    /**
     * Creates a new instance of DifferentialEvolutionConfig
     */
    public DifferentialEvolutionConfig() {
    }

    /**
     * returns JPanel with a slider
     * @param slider
     * @param multiplication
     * @param text
     */

    /**
     * function to pass the values of parameters to the unit
     */
    public int getDraw() {
        return 10;
    }

    /**
     * function to pass the values of parameters to the unit
     */
    public int getRec() {
        return 100;
    }


    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public int getMaxGenerationsWithoutChange() {
        return maxGenerationsWithoutChange;
    }

    public void setMaxGenerationsWithoutChange(int maxGenerationsWithoutChange) {
        this.maxGenerationsWithoutChange = maxGenerationsWithoutChange;
    }

}