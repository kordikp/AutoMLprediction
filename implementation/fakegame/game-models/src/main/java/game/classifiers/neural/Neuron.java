
package game.classifiers.neural;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents neuron
 *
 * @author Do Minh Duc
 */
public class Neuron implements Serializable {
    private IActivationFunction activationFunction;
    private NeuronType neuronType;
    private ArrayList<Synapse> incomingSynapses;
    private ArrayList<Synapse> outgoingSynapses;
    private NeuronValues previousValues;
    private NeuronValues currentValues;
    private NeuronLayer parentLayer;
    private int id;

    /**
     * Constructor, activation function is set to be sigmoid
     *
     * @param neuronType  type of neuron
     * @param parentLayer neuron layer where neuron belongs to
     * @param neuronId    neuron id, for debugging
     */
    public Neuron(NeuronType neuronType, NeuronLayer parentLayer, int neuronId) {
        this(neuronType, new ArrayList<Synapse>(), new ArrayList<Synapse>(), new ActivationFunctionSigmoid(), parentLayer, neuronId);
    }

    /**
     * Constructor
     *
     * @param neuronType         type of neuron
     * @param activationFunction neuron's activation function
     * @param parentLayer        neuron layer where neuron belongs to
     * @param neuronId           neuron id, for debugging
     */
    public Neuron(NeuronType neuronType, IActivationFunction activationFunction, NeuronLayer parentLayer, int neuronId) {
        this(neuronType, new ArrayList<Synapse>(), new ArrayList<Synapse>(), activationFunction, parentLayer, neuronId);
    }

    /**
     * Constructor
     *
     * @param neuronType         type of neuron
     * @param incomingSynapses   arraylist with incoming synapses
     * @param outgoingSynapses   arraylist with outgoing synapses
     * @param activationFunction neuron's activation function
     * @param parentLayer        neuron layer where neuron belongs to
     * @param neuronId           neuron id, for debugging
     */
    public Neuron(NeuronType neuronType, ArrayList<Synapse> incomingSynapses,
                  ArrayList<Synapse> outgoingSynapses, IActivationFunction activationFunction, NeuronLayer parentLayer, int neuronId) {
        this.neuronType = neuronType;
        this.incomingSynapses = incomingSynapses;
        this.outgoingSynapses = outgoingSynapses;
        this.activationFunction = activationFunction;
        this.currentValues = new NeuronValues();
        this.previousValues = new NeuronValues();
        this.parentLayer = parentLayer;
        this.id = neuronId;

    }

    /**
     * Returns neuron layer where neuron belongs to
     *
     * @return neuron layer where neuron belongs to
     */
    public NeuronLayer parentLayer() {
        return this.parentLayer;
    }

    public IActivationFunction activationFunction() {
        return this.activationFunction;
    }

    /**
     * Returns type of neuron
     *
     * @return type of neuron
     */
    public NeuronType type() {
        return this.neuronType;
    }

    /**
     * Sets type of neuron
     *
     * @param neuronType neuron's type
     */
    public void setType(NeuronType neuronType) {
        this.neuronType = neuronType;
    }

    /**
     * Returns arraylist with all incoming synapses
     *
     * @return arraylist with all incoming synapses
     */
    public ArrayList<Synapse> incomingSynapses() {
        return this.incomingSynapses;
    }

    /**
     * Sets new arraylist with incoming synapses
     *
     * @param incomingSynapses arraylist with incoming synapses to be set
     */
    public void setIncomingSynapses(ArrayList<Synapse> incomingSynapses) {
        this.incomingSynapses = incomingSynapses;
    }

    /**
     * Returns arraylist with all outgoing synapses
     *
     * @return arraylist with all outgoing synapses
     */
    public ArrayList<Synapse> outgoingSynapses() {
        return this.outgoingSynapses;
    }

    /**
     * Sets new arraylist with outgoing synapses
     *
     * @param outgoingSynapses arraylist with outgoing synapses
     */
    public void setOutgoingSynapses(ArrayList<Synapse> outgoingSynapses) {
        this.outgoingSynapses = outgoingSynapses;
    }

    /**
     * Adds incoming synapse. Should be called by synapse instance
     *
     * @param newIncomingSynapse incoming synapse to be added
     */
    public void addIncomingSynapse(Synapse newIncomingSynapse) {
        if (this.incomingSynapses.contains(newIncomingSynapse)) return;
        else {
            this.incomingSynapses.add(newIncomingSynapse);
        }
    }

    /**
     * Removes incoming synapse. Should be called by synapse instance.
     *
     * @param incomingSynapseToRemove incoming synapse to be removed
     */
    public void removeIncomingSynapse(Synapse incomingSynapseToRemove) {
        if (this.incomingSynapses.contains(incomingSynapseToRemove)) {
            this.incomingSynapses.remove(incomingSynapseToRemove);
        }
    }

    /**
     * Adds outgoing synapse. Should be called by synapse instance.
     *
     * @param newOutgoingSynapse outgoing synapse to be added
     */
    public void addOutgoingSynapse(Synapse newOutgoingSynapse) {
        if (this.outgoingSynapses.contains(newOutgoingSynapse)) return;
        else {
            this.outgoingSynapses.add(newOutgoingSynapse);
        }
    }

    /**
     * Removes outgoing synapse. Should be called by synapse instance.
     *
     * @param outgoingSynapseToRemove outgoing synapse to be removed
     */
    public void removeOutgoingSynapse(Synapse outgoingSynapseToRemove) {
        if (this.outgoingSynapses.contains(outgoingSynapseToRemove)) {
            this.outgoingSynapses.remove(outgoingSynapseToRemove);

        }
    }

    /**
     * Returns current value structure of neuron
     *
     * @return object holding values of neuron
     */
    public NeuronValues currentValues() {
        return this.currentValues;
    }

    /**
     * Returns previous value structure of neuron
     *
     * @return object holding values of neuron
     */
    public NeuronValues previousValues() {
        return this.previousValues;
    }

    /**
     * Returns current weighted sum of inputs
     */
    public double currentNetInput() {
        return this.currentValues.netInput;
    }

    /**
     * Sets weighted sum of inputs
     *
     * @param netInput weighted sum of inputs
     */
    public void setNetInput(double netInput) {
        this.currentValues.netInput = netInput;
    }

    /**
     * Calculates weighted sum of inputs
     *
     * @return weighted sum of inputs
     */
    public double calculateNetInput() {
        double netInput = 0;
        Iterator<Synapse> iterator = this.incomingSynapses.iterator();
        while (iterator.hasNext()) {
            Synapse synapse = iterator.next();
            netInput += synapse.weight() * synapse.transmittedValue();
        }
        return this.currentValues.netInput = netInput;
    }

    /**
     * Returns output value of neuron
     *
     * @return output value of neuron
     */
    public double currentOutput() {
        return this.currentValues.output;
    }

    /**
     * Sets output value of neuron
     *
     * @param outputValue output value to be set
     */
    public void setOutput(double output) {
        this.currentValues.output = output;
    }

    /**
     * Calculates output value of neuron
     *
     * @return output value of neuron
     */
    public double calculateOutput() {
        this.currentValues.output = this.calculateOutput(this.currentValues.netInput);
        return this.currentValues.output;
    }

    /**
     * Calculates output value of neuron for weighted sum of input passed in parameter.
     *
     * @param input weighted sum of inputs
     * @return
     */
    public double calculateOutput(double input) {
        this.currentValues.output = this.activationFunction.calculateOutput(input);
        return this.currentValues.output;
    }

    /**
     * Returns current value of delta
     *
     * @return current value of delta
     */
    public double currentDelta() {
        return this.currentValues.delta;
    }

    /**
     * Sets current value of delta
     *
     * @param value of delta to be set
     */
    public void setCurrentDelta(double delta) {
        this.currentValues.delta = delta;
    }

    /**
     * Calculates delta for output neurons
     *
     * @param desiredOutput desired value of outputs
     */
    public void calculateDelta(double desiredOutput) {
        this.calculateDelta(desiredOutput, this.activationFunction);
    }

    /**
     * Calculates delta for output neurons by using specified activation function
     *
     * @param desiredOutput      desired value of output
     * @param activationFunction activation function
     */
    public void calculateDelta(double desiredOutput, IActivationFunction activationFunction) {
        double derivative = activationFunction.calculateDerivative(this.currentNetInput());
        this.currentValues.delta = derivative * -(desiredOutput - this.currentValues.output);

    }

    /**
     * Calculates delta for hidden neuron
     */
    public void calculateDelta() {
        this.calculateDelta(this.activationFunction);
    }

    /**
     * Calculates delta for hidden neuron by using specified activation function
     *
     * @param activationFunction activation function
     */
    public void calculateDelta(IActivationFunction activationFunction) {
        double derivative = activationFunction.calculateDerivative(this.currentNetInput());
        double downStreamError = 0;
        Iterator<Synapse> iterator = this.outgoingSynapses.iterator();
        while (iterator.hasNext()) {
            Synapse outgoingSynapse = iterator.next();
            downStreamError += outgoingSynapse.weight() * outgoingSynapse.destinationNeuron().currentDelta();
        }

        this.currentValues.delta = derivative * downStreamError;
    }

    /**
     * Returns current value of derivative
     *
     * @return current value of derivative
     */
    public double currentDerivative() {
        return this.currentValues.derivative;
    }

    /**
     * Calculates derivative
     *
     * @return derivative
     */
    public double calculateDerivative() {
        this.currentValues.derivative = this.calculateDerivative(this.currentNetInput());
        return this.currentValues.derivative;
    }

    /**
     * Calculates derivative for input passed in parameter
     *
     * @param input weighted sum of input
     * @return derivative
     */
    public double calculateDerivative(double input) {
        this.currentValues.derivative = this.activationFunction.calculateDerivative(input);
        return this.currentValues.derivative;
    }

    /**
     * Calculates derivative by using specified activation function
     *
     * @param activationFunction
     * @return
     */
    public double calculateDerivative(IActivationFunction activationFunction) {
        this.currentValues.derivative = activationFunction.calculateDerivative(this.currentNetInput());
        return this.currentValues.derivative;
    }

    /**
     * Returns previous weighted sum of inputs
     *
     * @return previous weighted sum of inputs
     */
    public double getPreviousNetInput() {
        return this.previousValues.netInput;
    }

    /**
     * Returns previous output value of neuron
     *
     * @return previous output value of neuron
     */
    public double previousOutputValue() {
        return this.previousValues.output;
    }

    /**
     * Returns previous derivative
     *
     * @return previous derivative
     */
    public double previousDerivative() {
        return this.previousValues.derivative;
    }

    /**
     * Returns previous delta
     *
     * @return previous delta
     */
    public double previousDelta() {
        return this.previousValues.delta;
    }

    /**
     * Copies current values into object holding previous values
     */
    public void storeCurrentValues() {
        this.previousValues.netInput = this.currentValues.netInput;
        this.previousValues.output = this.currentValues.output;
        this.previousValues.derivative = this.currentValues.derivative;
        this.previousValues.delta = this.currentValues.delta;
    }

    /**
     * Resets current values
     */
    public void resetCurrentValues() {
        this.resetCurrentValues(0);
    }

    /**
     * Resets current values
     *
     * @param resetValue value to be reseted to
     */
    public void resetCurrentValues(double resetValue) {
        this.currentValues.netInput = resetValue;
        this.currentValues.derivative = resetValue;
        this.currentValues.netInput = resetValue;
        this.currentValues.output = resetValue;
    }

    /**
     * Returns neuron id
     *
     * @return neuron id
     */
    public int id() {
        return this.id;
    }
}
