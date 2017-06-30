
package game.classifiers.neural;

/**
 * Linear activation function
 *
 * @author Do Minh Duc
 */
public class ActivationFunctionLinear implements IActivationFunction {

    public double calculateOutput(double input) {
        return input;
    }

    public double calculateDerivative(double input) {
        return 1;
    }

    public String getType() {
        return "Linear";
    }

}
