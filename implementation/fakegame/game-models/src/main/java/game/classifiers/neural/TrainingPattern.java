package game.classifiers.neural;

import java.io.Serializable;

/**
 * This class represents one training pattern. It holds inputs pattern values
 * and desired outputs values.
 *
 * @author Do Minh Duc
 */
public class TrainingPattern implements Serializable {
    private Pattern inputPattern;
    private Pattern desiredOutputs;

    /**
     * Constructor
     *
     * @param inputPattern   input pattern
     * @param desiredOutputs desired outputs values
     */
    public TrainingPattern(Pattern inputPattern, Pattern desiredOutputs) {
        this.inputPattern = inputPattern;
        this.desiredOutputs = desiredOutputs;
    }

    /**
     * Constructor
     *
     * @param inputs         inputs values
     * @param desiredOutputs desired outputs values
     */
    public TrainingPattern(double[] inputs, double[] desiredOutputs) {
        this.inputPattern = new Pattern(inputs);
        this.desiredOutputs = new Pattern(desiredOutputs);
    }

    /**
     * Returns input pattern
     *
     * @return input pattern
     */
    public Pattern getInputPattern() {
        return this.inputPattern;
    }

    /**
     * Returns all desired outputs values
     *
     * @return all desired outputs values
     */
    public Pattern getDesiredOutputs() {
        return this.desiredOutputs;
    }

    /**
     * Returns desired value at specific output
     *
     * @param index position of output
     * @return desired value of output
     * @throws java.lang.Exception
     */
    public double getDesiredOutput(int index) throws Exception {
        if (index < 0 || index >= this.desiredOutputs.size())
            throw new Exception("TrainingPattern: getDesiredOutput: index of desired output out of range");
        return this.desiredOutputs.get(index);
    }

    /**
     * Sets desired value at specific output
     *
     * @param index position of output
     * @param value value to be set
     * @throws java.lang.Exception
     */
    public void setDesiredOutput(int index, double value) throws Exception {
        if (index < 0 || index >= this.desiredOutputs.size())
            throw new Exception("TrainingPattern: setDesiredOutput: index of desired output out of range");
        this.desiredOutputs.set(index, value);
    }

    /**
     * Returns specific input value
     *
     * @param index position of input
     * @return input value
     * @throws java.lang.Exception
     */
    public double getInput(int index) throws Exception {
        return this.inputPattern.get(index);
    }

    /**
     * Sets input value at specific position
     *
     * @param index position of input
     * @param value value be to set
     * @throws java.lang.Exception
     */
    public void setInput(int index, double value) throws Exception {
        this.inputPattern.set(index, value);
    }

    /**
     * Returns number of inputs in pattern
     *
     * @return number of inputs in pattern
     */
    public int inputsNumber() {
        return this.inputPattern.size();
    }

    /**
     * Returns number of outputs in pattern
     *
     * @return number of outputs in pattern
     */
    public int desiredOutputsNumber() {
        return this.desiredOutputs.size();
    }


}
