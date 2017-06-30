package configuration.game.trainers;

import configuration.AbstractCfgBean;

/**
 * User: kovaro1
 * Date: 7.9.2009
 */
public class PBILConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;
    private int maxIterations;          // maximum iterations of algorithm
    private int maxStagnation;          // maximum iterations without improvement
    private boolean debugOn;            // print debug info

    private double min;                 // parameter minimum
    private double max;                 // parameter maximum
    private int bitsPerVariable;     // number of bits for one variable representation
    private int populationSize;       // size of generated population
    private double learnRate;           // positive learning rate
    private double negLearnRate;        // negative learning rate
    private double mutProb;             // probability of mutation
    private double mutShift;            // shift caused by mutation


    /**
     * inicialises parametres to its default values
     */
    public PBILConfig() {
        maxIterations = 10000;
        maxStagnation = 100;
        debugOn = false;

        min = -10.0;
        max = 10.0;
        bitsPerVariable = 12;
        populationSize = 10;
        learnRate = 0.1;
        negLearnRate = 0.075;
        mutProb = 0.02;
        mutShift = 0.05;
    }


    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    public boolean getDebugOn() {
        return debugOn;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getBitsPerVariable() {
        return bitsPerVariable;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public double getLearnRate() {
        return learnRate;
    }

    public double getNegLearnRate() {
        return negLearnRate;
    }

    public double getMutProb() {
        return mutProb;
    }

    public double getMutShift() {
        return mutShift;
    }
}
