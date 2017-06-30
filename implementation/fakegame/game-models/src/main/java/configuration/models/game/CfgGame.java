package configuration.models.game;

import javax.validation.constraints.NotNull;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Range;

import configuration.models.game.algorithm.CfgDC;


/**
 * This is main configuration bean of the GAME algorithm
 */
@Component(name = "GAME algorithm", description = "basic properties of GAME algorithm")
public class CfgGame {

    @Property(name = "Compute neurons on multiple cores")
    private boolean multicore = false;

    @Property(name = "Linear neurons only")
    private boolean linear = false;

    @Property(name = "Fast neurons only")
    private boolean fast = true;

    @Property(name = "Complexity of the algorithm")
    @Range(from = 1, to = 100)
    private int c = 10;
    @Property(name = "Genetic algorithm")
    transient private CfgDC algorithm = new CfgDC();
    @Property(name = "Neurons")
    transient private CfgNeurons neurons = new CfgNeurons();
    @Property(name = "Trainers")
    transient private CfgTrainers trainers = new CfgTrainers();
    @Property(name = "true = Growing number of inputs to units, false = Number of inputs not limited")
    private boolean justTwo = false;


    @Property(name = "Build layers while error decreeses (false= stop when the decrease is too small)")
    private boolean buildWhileDec = false;
    @Property(name = "Delete neurons with accuracy worse than previous layer elite")
    private boolean deleteWorse = true;
    @Property(name = "Use bootstrap sampling (othewise training vectors are selected without replacement")
    private boolean bootstrap = false;
    @Property(name = "Offsprings of units [(0=type and trainer derived from parents)...(100=random)]:")
    @Range(from = 0, to = 100)
    private int randomChildren = 50;


    @Property(name = "Max vectors in Training set", description = "Maximum number of vectors used for training")
    private int maxTrainSetSize;

    @Property(name = "Vectors in Testing set", description = "Percent of the data set used for testing")
    private int vectorsInTestingSet; //percent of vectors in the testing set

    @Property(name = "Regularization of neurons (complexity penalty is part of the validation error")
    @NotNull
    private ValidationError regularization = ValidationError.RMS;

    public static enum ValidationError {
        RMS, RMS_PENALTY, RMS_PENALTY_NOISE
    }

    public final static int MAX_LAYERS = 20;
    final static int LEARNING_RECORDS = 50;
    public final static int MAX_UNITS_USED = 500;

    public CfgDC getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(CfgDC algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isMulticore() {
        return multicore;
    }

    public void setMulticore(boolean multicore) {
        this.multicore = multicore;
    }

    public CfgNeurons getNeurons() {
        return neurons;
    }

    public int getVectorsInTestingSet() {
        return vectorsInTestingSet;
    }

    public void setVectorsInTestingSet(int vectorsInTestingSet) {
        this.vectorsInTestingSet = vectorsInTestingSet;
    }

    public void setNeurons(CfgNeurons neurons) {
        this.neurons = neurons;
    }

    public int getMaxTrainSetSize() {
        return maxTrainSetSize;
    }

    public void setMaxTrainSetSize(int maxTrainSetSize) {
        this.maxTrainSetSize = maxTrainSetSize;
    }

    public CfgTrainers getTrainers() {
        return trainers;
    }

    public void setTrainers(CfgTrainers trainers) {
        this.trainers = trainers;
    }

    public boolean isJustTwo() {
        return justTwo;
    }

    public void setJustTwo(boolean justTwo) {
        this.justTwo = justTwo;
    }

    public boolean isBuildWhileDec() {
        return buildWhileDec;
    }

    public void setBuildWhileDec(boolean buildWhileDec) {
        this.buildWhileDec = buildWhileDec;
    }

    public boolean isDeleteWorse() {
        return deleteWorse;
    }

    public void setDeleteWorse(boolean deleteWorse) {
        this.deleteWorse = deleteWorse;
    }

    public boolean isBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }

    public int getRandomChildren() {
        return randomChildren;
    }

    public void setRandomChildren(int randomChildren) {
        this.randomChildren = randomChildren;
    }

    public ValidationError getRegularization() {
        return regularization;
    }

    public void setRegularization(ValidationError regularization) {
        this.regularization = regularization;
    }


    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }

    public boolean isLinear() {
        return linear;
    }

    public void setLinear(boolean lin) {
        this.linear = lin;
    }
}
