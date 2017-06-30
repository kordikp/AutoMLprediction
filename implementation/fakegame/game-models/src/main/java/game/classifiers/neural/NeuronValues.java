
package game.classifiers.neural;

import java.io.Serializable;

/**
 * Holds neuron values
 *
 * @author Do Minh Duc
 */
public class NeuronValues implements Serializable {
    public double output;
    public double derivative;
    public double netInput;
    public double delta;

    /**
     * Constructor
     */
    NeuronValues() {
        this.output = 0;
        this.derivative = 0;
        this.netInput = 0;
        this.delta = 0;
    }

    /**
     * Constructor
     *
     * @param value      neuron output value
     * @param derivative neuron derivative
     * @param netInput   neuron weighted sum of input
     * @param delta      neuron delta
     */
    NeuronValues(double value, double derivative, double netInput, double delta) {
        this.output = value;
        this.derivative = derivative;
        this.netInput = netInput;
        this.delta = delta;
    }

}
