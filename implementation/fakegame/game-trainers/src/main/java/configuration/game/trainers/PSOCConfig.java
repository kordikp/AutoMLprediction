package configuration.game.trainers;

import configuration.AbstractCfgBean;

/**
 * @author weberj1
 */
public class PSOCConfig extends AbstractCfgBean {
    private static final double DEFAULT_PHI = 3;

    int maxIterations;             // maximal number of method's outer loop iterations
    int maxStagnation;             // maximum stagnation
    int populationSize;            // number of ants in population
    double initMin;                // minimum for random initialization
    double initMax;                // maximum for random initialization
    double phi1;                   // cognitive acceleration coefficient
    double phi2;                   // social acceleration coefficient
    double k;                      // constant used to compute chi
    boolean debugOn;               // debug flag
    double chi;                    // auxillary constant, is defined by phi1, phi2 and k variables

    public PSOCConfig() {

        maxIterations = 500; // maximal number of method's outer loop iterations
        maxStagnation = 100;             // maximum stagnation
        populationSize = 10;            // number of ants in population
        initMin = -10.0;             // minimum for random initialization
        initMax = 10.0;              // maximum for random initialization
        phi1 = 2.0;                  // cognitive acceleration coefficient
        phi2 = 2.0;                  // social acceleration coefficient
        k = 1;                       // constant used to compute chi
        computeChi();
        debugOn = false;
    }


    private void computeChi() { //throws ParticleSwarmOptimizationCanonicalException{
        //    if ((phi1 + phi2) <= 4)
        //        throw new ParticleSwarmOptimizationCanonicalException("Sum of cognitive acceleration coefficient and social acceleration coefficient must be greater than 4.");
        double phi = phi1 + phi2;
        double abs = 2 - phi - Math.sqrt((phi * phi) - (4 * phi));  // auxiliary variables used for the sake of legibility

        chi = 2 * k / Math.abs(abs);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    public void setMaxStagnation(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getInitMin() {
        return initMin;
    }

    public void setInitMin(double initMin) {
        this.initMin = initMin;
    }

    public double getInitMax() {
        return initMax;
    }

    public void setInitMax(double initMax) {
        this.initMax = initMax;
    }

    public double getPhi1() {
        return phi1;
    }

    public void setPhi1(double phi1) {
        this.phi1 = phi1;
    }

    public double getPhi2() {
        return phi2;
    }

    public void setPhi2(double phi2) {
        this.phi2 = phi2;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }

    public double getChi() {
        return chi;
    }

    public void setChi(double chi) {
        this.chi = chi;
    }
}


