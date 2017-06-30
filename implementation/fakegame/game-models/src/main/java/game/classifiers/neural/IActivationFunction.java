
package game.classifiers.neural;

import java.io.Serializable;

/**
 * Interface for activation function
 *
 * @author Do Minh Duc
 */
public interface IActivationFunction extends Serializable {
    /**
     * Calculates output value of neuron
     *
     * @param input input
     * @return output value
     */

    public double calculateOutput(double input);

    /**
     * Calculates derivative of neuron
     *
     * @param input input
     * @return derivative
     */
    public double calculateDerivative(double input);

    public String getType();
}
