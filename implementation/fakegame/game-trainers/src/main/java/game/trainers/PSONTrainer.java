package game.trainers;

import game.trainers.stopping.StagnationStopCondition;
import game.trainers.gradient.Newton.Uncmin_methods;

import java.util.Random;

import configuration.game.trainers.PSOConfig;

/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.0
 *          <p>
 *          Title:       Particle Swarm Optimization (PSO)
 *          Description: original PSO algorithm
 *          Source:      A Comparison of Particle Swarm Optimization Algorithms Based on Run-Length Distributions
 *          Oca, Stutzle, Birattari, Dorigo
 *          in
 *          Proceedings of ANTS 2006 (Dorigo et al.): Ant Colony Optimization and Swarm Intelligence
 */

public class PSONTrainer extends Trainer implements Uncmin_methods {

    transient public StagnationStopCondition stopCondition;
    protected int iteration;

    protected double[] x;
    protected double fx;
    int populationSize;
    int maxStagnation;
    int maxIterations;
    double initMax;
    double initMin;
    double phi1;                   // cognitive acceleration coefficient
    double phi2;                   // social acceleration coefficient
    double chi;                    // auxillary constant, is defined by phi1, phi2 and k variables

    private double v_max = 10.0;
    transient Random generator;

    protected class Particle {
        public double x[];  // position
        public double v[];  // velocity
        public double p[];  // best position in particle's history
        public double fp;   // best position function values

        Particle(int dimension) {
            x = new double[dimension];
            p = new double[dimension];
            v = new double[dimension];
        }
    }

    public PSONTrainer() {
        generator = new Random();
    }

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        PSOConfig config = (PSOConfig) cfg;

        populationSize = config.getPopulationSize();
        maxStagnation = config.getMaxStagnation();
        maxIterations = config.getMaxIterations();
        initMin = config.getInitMin();
        initMax = config.getInitMax();
        phi1 = config.getPhi1();
        phi2 = config.getPhi2();

        v_max = (initMax - initMin);
        this.stopCondition = new StagnationStopCondition(maxStagnation);
    }

    public void teach() {
        //  fireOptimizationStart();

        x = new double[coefficients];              // best solution
        fx = Double.POSITIVE_INFINITY;  // best solution value

        // initialize particles
        Particle particles[] = new Particle[populationSize];
        for (int i = 0; i < populationSize; i++) {
            particles[i] = new Particle(coefficients);
            for (int d = 0; d < coefficients; d++) {
                particles[i].x[d] = (generator.nextDouble() * (initMax - initMin)) + initMin;
                particles[i].v[d] = 2.0 * (generator.nextDouble() - 0.5) * (initMax - initMin); // (generator.nextDouble()-0.5)/10;
                particles[i].fp = Double.POSITIVE_INFINITY;
            }
        }

        //initialize stop condition
        if (stopCondition != null) {
            stopCondition.init(fx);
        }

        //iterate until solution found or maxIterations exceeded
        for (iteration = 1; iteration <= maxIterations; iteration++) {
            //   fireIterationStart();

            for (int i = 0; i < populationSize; i++) {

                // evaluate error
                double error = f_to_minimize(particles[i].x);
                if (error < particles[i].fp) {
                    // particle's best solution
                    System.arraycopy(particles[i].x, 0, particles[i].p, 0, coefficients);
                    particles[i].fp = error;
                    if (error < fx) {
                        // swarm best solution
                        System.arraycopy(particles[i].x, 0, x, 0, coefficients);
                        fx = error;
                    }
                }

                // update particles
                updateParticles(particles, i);

            }

            //test for convergence
            if (stopCondition != null) {
                stopCondition.set(fx);
            }

            if (stopCondition != null && stopCondition.stop()) {
                //            fireIterationEnd();
                //             fireOptimizationEnd();
                return;
            }

            //        fireIterationEnd();
        }

    }

    protected void updateParticles(Particle[] particles, int i) {
        for (int d = 0; d < coefficients; d++) {
            // velocity
            particles[i].v[d] =
                    particles[i].v[d] +
                            phi1 * generator.nextDouble() * (particles[i].p[d] - particles[i].x[d]) +
                            phi2 * generator.nextDouble() * (x[d] - particles[i].x[d]);
            // velocity limit
            if (particles[i].v[d] > v_max) particles[i].v[d] = v_max;
            else if (particles[i].v[d] < -v_max) particles[i].v[d] = -v_max;

            // position
            particles[i].x[d] += particles[i].v[d];
        }
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "Particle Swarm Optimization";
    }


    public Class getConfigClass() {
        return PSOConfig.class;
    }

    public double f_to_minimize(double[] x) {
        return getAndRecordError(x, 10, 100, true);
    }

    public void gradient(double[] x, double[] g) {
        unit.gradient(x, g);
    }

    public void hessian(double[] x, double[][] h) {
        unit.hessian(x, h);
    }

    public boolean allowedByDefault() {
        return false;
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
