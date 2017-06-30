
package game.classifiers.neural;

import java.io.Serializable;

/**
 * Represents synapse
 *
 * @author Do Minh Duc
 */
public class Synapse implements Serializable {
    private Neuron sourceNeuron;
    private Neuron destinationNeuron;
    private double weight;
    private double currentSlope;
    private double previousSlope;
    private double lastWeightChange;
    private double stepSize;

    /**
     * Constructor
     *
     * @param sourceNeuron      source neuron
     * @param destinationNeuron destination neuron
     */
    public Synapse(Neuron sourceNeuron, Neuron destinationNeuron) {
        this(sourceNeuron, destinationNeuron, 0);
    }

    /**
     * Constructor
     *
     * @param sourceNeuron      source neuron
     * @param destinationNeuron destination neuron
     * @param weight            weight
     */
    public Synapse(Neuron sourceNeuron, Neuron destinationNeuron, double weight) {
        this.sourceNeuron = sourceNeuron;
        this.destinationNeuron = destinationNeuron;
        this.weight = weight;
        //error shouldn't happen
        this.sourceNeuron.addOutgoingSynapse(this);
        this.destinationNeuron.addIncomingSynapse(this);
        this.lastWeightChange = 0;
        this.currentSlope = 0;
        this.previousSlope = 0;
        this.stepSize = 0;
    }

    /**
     * Sets step size used in Rprop
     *
     * @param stepSize
     */
    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * Returns step size used in Rprop
     *
     * @return
     */
    public double stepSize() {
        return this.stepSize;
    }

    /**
     * Generates weight randomly
     */
    public void generateWeight() {
        //this.weight = Math.random();
        generateWeight(-1, 1);
        //this.weight = -0.1;

    }

    /**
     * Generates randomly weight within specified range (including lower bound excluding upper boung)
     *
     * @param lowerBound range down limit
     * @param upperBound range upper
     * @return
     */
    public boolean generateWeight(double lowerBound, double upperBound) {
        if (lowerBound >= upperBound || upperBound <= 0) return false;
        this.weight = Math.random() * (upperBound - lowerBound) + lowerBound;
        return true;
    }

    /**
     * Returns source neuron
     *
     * @return source neuron
     */
    public Neuron sourceNeuron() {
        return this.sourceNeuron;
    }

    /**
     * Returns destination neuron
     *
     * @return destination neuron
     */
    public Neuron destinationNeuron() {
        return this.destinationNeuron;
    }

    /**
     * Returns synapse weight
     *
     * @return weight
     */
    public double weight() {
        return this.weight;
    }

    /**
     * Sets synapse weight, also sets value for last weight change
     *
     * @param weight weight
     */
    public void setWeight(double weight) {
        this.lastWeightChange = weight - this.weight;
        this.weight = weight;
    }

    /**
     * Adds value specified in parameter to weight
     *
     * @param weightChange value to be added
     */

    public void addWeight(double weightChange) throws Exception {
        if (Double.isInfinite(weightChange) || Double.isNaN(weightChange))
            throw new Exception("class Synapse: method addWeight: parameter weightChange not a number");
        this.lastWeightChange = weightChange;
        this.weight += weightChange;
    }

    /**
     * Returns transmitted value
     *
     * @return transmitted value
     */
    public double transmittedValue() {
        return this.sourceNeuron.currentOutput();
    }

    /**
     * Returns last change of weight
     *
     * @return last change of weight
     */
    public double lastWeightChange() {
        return this.lastWeightChange;
    }

    /**
     * Sets last change of weight
     *
     * @param weightChange value to be set
     */
    public void setLastWeightChange(double weightChange) {
        this.lastWeightChange = weightChange;
    }

    /**
     * Removes synapse from source and destination neurons
     */
    public void remove() {
        this.sourceNeuron.removeOutgoingSynapse(this);
        this.destinationNeuron.removeIncomingSynapse(this);
    }

    /**
     * Returns partial derivative
     *
     * @return partial derivative
     */
    public double currentSlope() {
        return this.currentSlope;
    }

    /**
     * Sets partial derivative
     *
     * @param partialDerivative partial derivative to be set
     */
    public void setCurrentSlope(double partialDerivative) {
        this.currentSlope = partialDerivative;
    }

    /**
     * Returns previous partial derivative
     *
     * @return previous partial derivative
     */
    public double previousSlope() {
        return this.previousSlope;
    }

    /**
     * Sets previous partial derivative
     *
     * @param previousSlope value to be set
     */
    public void setPreviousSlope(double previousSlope) {
        this.previousSlope = previousSlope;
    }

    /**
     * Copies current partial derivative into object holding previous values
     */
    public void storeCurrentSlope() {
        this.previousSlope = this.currentSlope;
    }
}
