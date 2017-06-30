package configuration.classifiers.single;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import configuration.classifiers.ClassifierConfigBase;

/**
 * Configures the cascade correlation neural network
 */
@Component(name = "NeuralClassifierConfig", description = "Cascade Correlation network configuration")
public class NeuralClassifierConfig extends ClassifierConfigBase {

    @Property(name = "Acceptable error", description = "Learning terminated when bellow this error")
    private double acceptableError;
    private int maxNumberLayer;
    private int candMaxUpdateCycles;
    private double minCorrGrowth;


    private double candEpsilon;
    private double candMaxAlfa;
    private double candDecay;

    private int outMaxUpdateCycles;
    private double minErrRed;
    private double outEpsilon;
    private double outMaxAlfa;
    private double outDecay;

    public NeuralClassifierConfig() {
        super();
        acceptableError = 0.001;
        maxNumberLayer = 10;
        candMaxUpdateCycles = 400;
        minCorrGrowth = 0.25;
        candEpsilon = 0.35;
        candMaxAlfa = 2;
        candDecay = 0.0;

        outMaxUpdateCycles = 400;
        minErrRed = 0.25;
        outEpsilon = 0.35;
        outMaxAlfa = 2;
        outDecay = 0.0001;
    }

    public double getAcceptableError() {
        return acceptableError;
    }

    public void setAcceptableError(double acceptableError) {
        this.acceptableError = acceptableError;
    }

    public int getMaxNumberLayer() {
        return maxNumberLayer;
    }

    public void setMaxNumberLayer(int maxNumberLayer) {
        this.maxNumberLayer = maxNumberLayer;
    }

    public int getCandMaxUpdateCycles() {
        return candMaxUpdateCycles;
    }

    public void setCandMaxUpdateCycles(int candMaxUpdateCycles) {
        this.candMaxUpdateCycles = candMaxUpdateCycles;
    }

    public double getMinCorrGrowth() {
        return minCorrGrowth;
    }

    public void setMinCorrGrowth(double minCorrGrowth) {
        this.minCorrGrowth = minCorrGrowth;
    }

    public double getCandEpsilon() {
        return candEpsilon;
    }

    public void setCandEpsilon(double candEpsilon) {
        this.candEpsilon = candEpsilon;
    }

    public double getCandMaxAlfa() {
        return candMaxAlfa;
    }

    public void setCandMaxAlfa(double candMaxAlfa) {
        this.candMaxAlfa = candMaxAlfa;
    }

    public double getCandDecay() {
        return candDecay;
    }

    public void setCandDecay(double candDecay) {
        this.candDecay = candDecay;
    }

    public int getOutMaxUpdateCycles() {
        return outMaxUpdateCycles;
    }

    public void setOutMaxUpdateCycles(int outMaxUpdateCycles) {
        this.outMaxUpdateCycles = outMaxUpdateCycles;
    }

    public double getMinErrRed() {
        return minErrRed;
    }

    public void setMinErrRed(double minErrRed) {
        this.minErrRed = minErrRed;
    }

    public double getOutEpsilon() {
        return outEpsilon;
    }

    public void setOutEpsilon(double outEpsilon) {
        this.outEpsilon = outEpsilon;
    }

    public double getOutMaxAlfa() {
        return outMaxAlfa;
    }

    public void setOutMaxAlfa(double outMaxAlfa) {
        this.outMaxAlfa = outMaxAlfa;
    }

    public double getOutDecay() {
        return outDecay;
    }

    public void setOutDecay(double outDecay) {
        this.outDecay = outDecay;
    }
}