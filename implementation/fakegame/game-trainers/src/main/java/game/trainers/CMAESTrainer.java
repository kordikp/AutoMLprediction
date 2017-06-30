package game.trainers;

import game.trainers.cmaes.CMAEvolutionStrategy;
import game.trainers.cmaes.fitness.IObjectiveFunction;
import configuration.game.trainers.CMAESConfig;

/**
 * User: drchaj1
 * Cmaes optimization strategy
 */
public class CMAESTrainer extends Trainer implements IObjectiveFunction {
    private static final long serialVersionUID = 1L;
    int rec, draw;
    private CMAEvolutionStrategy cma;

    public CMAESTrainer() {
        cma = new CMAEvolutionStrategy();
        cma.options.verbosity = -1;
        cma.setInitialX(0.0);
    }

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        CMAESConfig cf = (CMAESConfig) cfg;

        rec = cf.getRec();
        draw = cf.getDraw();
        cma.setInitialStandardDeviation(cf.getInitialStandardDeviation());
        cma.options.stopMaxFunEvals = cf.getStopMaxFunEvals();
        cma.options.stopFitness = cf.getStopFitness();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        cma.setDimension(coefficients);
    }

    @Override
    public void teach() {

        // initialize cma and get fitness array to fill in later
        double[] fitness = cma.init();

        while (cma.stopConditions.getNumber() == 0) {

            // --- core iteration step ---
            double[][] pop = cma.samplePopulation(); // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                fitness[i] = valueOf(pop[i]); // fitfun.valueOf() is to be minimized
            }
            cma.updateDistribution(fitness);         // pass fitness array to update search distribution
            // --- end core iteration step ---
        }
        cma.setFitnessOfMeanX(valueOf(cma.getMeanX())); // updates the best ever solution
    }

    @Override
    public String getMethodName() {
        return "CMA-ES";
    }


    @Override
    public Class getConfigClass() {
        return CMAESConfig.class;
    }

    @Override
    public boolean isExecutableInParallelMode() {
        return true;
    }

    // CMAES IObjectiveFunction
    public double valueOf(double[] x) {
        return getAndRecordError(x, rec, draw, true);
    }

    public boolean isFeasible(double[] x) {
        return true;
    }
}
