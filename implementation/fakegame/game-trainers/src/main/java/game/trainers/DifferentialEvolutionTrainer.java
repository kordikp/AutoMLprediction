/*
 * DifferentialEvolutionTrainer.java
 * - represents evolution optimalization algorithm - Differential Evolution
 * Created on 21. June 2006, 10:38
 */

package game.trainers;

import game.trainers.gradient.DifferentialEvolution.Individual;
import configuration.game.trainers.DifferentialEvolutionConfig;

/**
 * @author Miroslav Janosik (janosm2@fel.cvut.cz)
 */
public class DifferentialEvolutionTrainer extends Trainer {

    /*
     * DE parametrs - settings
     */
    // NP ... Number of Parents (size of population)
    private int NP; // recommanded <4, 100>; optimum 10+
    // F ... mutation constant
    private double F; // recommanded <0,2>; optimum <0.3, 0.9>
    // CR ... Crossover Rating
    private double CR; // recommanded <0,1>; optimum <0.8, 0.9>
    // GEN ... maximal generations
    private int GEN; // depends on user
    // D ... Dimension of vector of each APAIndividual
    private int D; // depends on neuron network

    // defines howmany generations should alg. continue if the bestError is the same
    private int MAXGENWITHOUTCHANGE;
    // for ramdom setting in first generation
    private double MIN;
    private double MAX;

    /*
     * instance varriables for DE algorithm
     */ private int generationCounter;
    private double previousBestError;
    private int generationWithoutChage;
    private transient Individual[] population;
    private transient Individual[] nextPopulation;

    /**
     * returns actual error for a vector x
     */
 /*   public double getError(double[] x) {
        try{
            ((Neuron)unit).setProgress((int) (100 * (this.generationCounter / this.GEN)));
        }catch(ClassCastException e){} //consume exception when unit is model
        return getAndRecordError(x,this.cf.getRec(),this.cf.getDraw(),false);
    }
 */

    /**
     * name of method fro GUI
     */
    public String getMethodName() {
        return "Differential Evolution";
    }

    /**
     * sets the configuranion class
     */


    /**
     * name of configuration class
     */
    public Class getConfigClass() {
        return DifferentialEvolutionConfig.class;
    }

    /* can be overriden... */
    public boolean allowedByDefault() {
        return super.allowedByDefault();
    }

    public DifferentialEvolutionTrainer() {
        this.MIN = -DifferentialEvolutionConfig.INITMAX;
        this.MAX = DifferentialEvolutionConfig.INITMAX;
        this.generationCounter = 0;
        this.previousBestError = Double.MAX_VALUE;
        this.generationWithoutChage = 0;
    }

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        DifferentialEvolutionConfig cf = (DifferentialEvolutionConfig) cfg;

        this.NP = cf.getPopulationSize();
        this.F = cf.getMutationRate();
        this.CR = cf.getCrossoverRate();
        this.GEN = cf.getMaxGenerations();
        this.MAXGENWITHOUTCHANGE = cf.getMaxGenerationsWithoutChange();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        this.D = coef;

    }

    /**
     * standard toString() method
     */
    public String toString() {
        return "\t{ NP: " + this.NP + "\tF: " + this.F + "\tCR: " + this.CR + "\tGEN: " + this.GEN + "\tMAXw/oC: " + this.MAXGENWITHOUTCHANGE + "\tMIN: " + this.MIN + "\tMAX: " + this.MAX + " }";
    }

    /**
     * starts teaching process
     */
    public void teach() {
        // initialize the first random population
        this.initPopulation();

        // maximal GEN populations
        while (this.generationCounter < this.GEN) {

            // Reproduction of each APAIndividual
            for (int i = 0; i < population.length; i++) {
                this.makeReproduction(i);
            }

            // swap populations
            this.population = this.nextPopulation;
            this.nextPopulation = new Individual[this.NP];

            // increment generation counter
            this.generationCounter++;

            // test - is the solution going better...?
            if (!isSolutionGoingBetter()) {
                break;
            }
        }
        /* only for testing ... result of teaching process */
        /*
        System.out.println(" DE end: generation_counter: " + this.generationCounter );
        String vect = "";
        for (int i = 0; i < this.best.length; i++) {
            vect += "\t" + this.best[i] + ";";
        }
        System.out.println("  - best vector: " + this.errorBestSoFar + " ||| [" + vect + "]");
         */
    }

    /* checks if the solution is going better for last n generations */
    private boolean isSolutionGoingBetter() {
        if (this.errorBestSoFar == this.previousBestError) {
            this.generationWithoutChage++;
        } else {
            this.previousBestError = this.errorBestSoFar;
            this.generationWithoutChage = 0;
        }
        return this.generationWithoutChage < this.MAXGENWITHOUTCHANGE;
    }

    /**
     * initialize the first random population
     */
    private void initPopulation() {
        this.population = new Individual[this.NP];
        for (int i = 0; i < this.population.length; i++) {
            this.population[i] = new Individual(this.D);
            this.population[i].setRandomValues(this.MIN, this.MAX);
            this.population[i].setCostValue(this.getError(this.population[i].getValues()));
        }
        this.nextPopulation = new Individual[this.NP];
    }

    /**
     * main method of DE - makes new individual for next genneration
     *
     * @param ind index
     */
    private void makeReproduction(int ind) {
        // setting parents
        Individual r0 = this.population[ind];
        int index[] = this.setParentsIndexes(ind);
        Individual r1 = this.population[index[0]];
        Individual r2 = this.population[index[1]];
        Individual r3 = this.population[index[2]];

        // setting mutation vector: v = (r1 - r2)*F + r3
        Individual v = r1.minus(r2);
        v = v.timesScalar(this.F);
        v = v.plus(r3);

        // makeing crossover -> setting test vector
        for (int i = 0; i < this.D; i++) {
            if (Math.random() > this.CR) {
                v.setValueAt(i, r0.getValueAt(i));
            }
        }

        // set the costValue of new vector v (after mutation and crossover)
        double costValue = this.getError(v.getValues());
        v.setCostValue(costValue);

        // test -> is new individual better then it's parent ...?
        // '<' ... because we want to minimalize the function (error in neural network)
        if (v.getCostValue() < r0.getCostValue()) {
            this.nextPopulation[ind] = v; // new solution is better
        } else {
            this.nextPopulation[ind] = r0; // old solution is better
        }
    }

    // sets parents indexes. main parent is the i-th one in population
    private int[] setParentsIndexes(int i) {
        // makeing indexes for other 3 parents
        int index[] = {-1, -1, -1};
        int current = 0;
        while (current < 3) {
            int ind = (int) Math.round(this.NP * Math.random());
            // we must select 3 differend parrents
            if ((ind != i) && (ind != this.NP) && (ind != index[0]) && (ind != index[1]) && (ind != index[2])) {
                index[current] = ind;
                current++;
            }
        }
        return index;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return true;
    }
}
