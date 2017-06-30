package configuration.models.game.algorithm;


import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Range;

/**
 * This is the configuration bean of the deterministic crowding algorithm running
 * in each layer of the GAME network
 */
@Component(name = "Core of the Algorithm", description = "Expert options of the genetic algorithm in GAME")
public class CfgDC {
    @Property(name = "Deterministic crowding employed, false = standard GA")
    private boolean crowdingEmployed = true;
    @Property(name = "Algorithm terminated when diversity decreases bellow following fraction of initial diversity")
    @Range(from = .005, to = .9)
    private double maximalDiversityDrop = 0.05;
    @Property(name = "Mutation rate in the deterministic crowding algorithm")
    @Range(from = 0, to = 1)
    private double mutationRate = 0.8;
    @Property(name = "Distance significance - without distance DC becomes standard GA")
    @Range(from = 0, to = 500)
    private double distanceMatters = 10;
    @Property(name = "Maximum number of surviving neurons in a layer")
    @Range(from = 1, to = 50)
    private int layerNNum = 3;
    @Property(name = "Initial population of neurons in a layer")
    @Range(from = 1, to = 500)
    private int layerINum = 15;
    @Property(name = "Number of epochs of the genetic algorithm (DC)")
    @Range(from = 0, to = 500)
    private int ep = 30;
    @Property(name = "Validate (compute fitness) on both learning and validation data")
    private boolean testOnBothTrainingAndTestingData = false;
    @Property(name = "Maximal number of training vectors used for learning")
    @Range(from = 1, to = 100000)
    private int learnPercent = 200;
    @Property(name = "Genotypic distance is included")
    private boolean genomeDistance = true;
    @Property(name = "Correlation of errors is included as part of the distance")
    private boolean correlationDistance = true;


    public boolean isCrowdingEmployed() {
        return crowdingEmployed;
    }

    public void setCrowdingEmployed(boolean crowdingEmployed) {
        this.crowdingEmployed = crowdingEmployed;
    }

    public double getMaximalDiversityDrop() {
        return maximalDiversityDrop;
    }

    public void setMaximalDiversityDrop(double maximalDiversityDrop) {
        this.maximalDiversityDrop = maximalDiversityDrop;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public double getDistanceMatters() {
        return distanceMatters;
    }

    public void setDistanceMatters(double distanceMatters) {
        this.distanceMatters = distanceMatters;
    }

    public int getLayerNNum() {
        return layerNNum;
    }

    public void setLayerNNum(int layerNNum) {
        this.layerNNum = layerNNum;
    }

    public int getLayerINum() {
        return layerINum;
    }

    public void setLayerINum(int layerINum) {
        this.layerINum = layerINum;
    }

    public int getEp() {
        return ep;
    }

    public void setEp(int ep) {
        this.ep = ep;
    }

    public boolean isTestOnBothTrainingAndTestingData() {
        return testOnBothTrainingAndTestingData;
    }

    public void setTestOnBothTrainingAndTestingData(boolean testOnBothTrainingAndTestingData) {
        this.testOnBothTrainingAndTestingData = testOnBothTrainingAndTestingData;
    }

    public int getLearnPercent() {
        return learnPercent;
    }

    public void setLearnPercent(int learnPercent) {
        this.learnPercent = learnPercent;
    }

    public boolean isGenomeDistance() {
        return genomeDistance;
    }

    public void setGenomeDistance(boolean genomeDistance) {
        this.genomeDistance = genomeDistance;
    }

    public boolean isCorrelationDistance() {
        return correlationDistance;
    }

    public void setCorrelationDistance(boolean correlationDistance) {
        this.correlationDistance = correlationDistance;
    }
}