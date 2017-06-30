
package game.classifiers.neural;

/**
 * Sigmoid activation function
 *
 * @author Do Minh Duc
 */
public class ActivationFunctionSigmoid implements IActivationFunction {
    private int steepness;

    public ActivationFunctionSigmoid() {
        this(1);
    }

    public String getType() {
        return "Sigmoid";
    }

    public ActivationFunctionSigmoid(int steepness) {
        super();
        this.steepness = steepness;
    }

    private double calculateSigmoid(double input) {
        //if (input > 15) return 1;
        //if (input < -15) return 0;
        return (1 / (1 + Math.exp(-input * this.steepness)));
    }


    public double calculateOutput(double input) {
        return calculateSigmoid(input);
    }

    public double calculateDerivative(double input) {
        return calculateSigmoid(input) * (1 - calculateSigmoid(input));
    }

}
