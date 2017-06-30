
package game.classifiers.neural;

/**
 * Sigmoid activation function with the use of offset suggested by Fahlman
 *
 * @author Do Minh Duc
 */
public class ActivationFunctionSigmoidFahlmanOffset implements IActivationFunction {

    private double offset;
    private static final double DEFAULT_OFFSET = 0.1;

    /**
     * Constructor
     */
    public ActivationFunctionSigmoidFahlmanOffset() {
        this.offset = DEFAULT_OFFSET;
    }

    public String getType() {
        return "Fahlman offset sigmoid";
    }

    /**
     * Constructor
     *
     * @param offset offset
     */
    public ActivationFunctionSigmoidFahlmanOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Sets offset
     *
     * @param offset offset to be set
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Returns offset
     *
     * @return
     */
    public double getOffset() {
        return this.offset;
    }

    private double calculateSigmoid(double input) {
        if (input > 15) return 1;
        if (input < -15) return 0;
        return (1 / (1 + Math.exp(-input)));
    }

    public double calculateOutput(double input) {
        return this.calculateSigmoid(input);
    }

    public double calculateDerivative(double input) {
        return calculateSigmoid(input) * (1 - calculateSigmoid(input)) + offset;

    }
}
