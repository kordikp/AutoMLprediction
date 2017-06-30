package game.data;

import java.io.Serializable;

/**
 * This class helps to normalize data internally for ConnectableClassifiers and ConnectableModels
 */
public abstract class DataNormalizer implements Serializable {
    transient protected double[][] inputs;
    transient protected double[][] outputs;

    public void init(double[][] inputs, double[][] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public void init(double[][] inputs, double[] outputs) {
        double[][] out = new double[1][];
        out[0] = outputs;
        this.init(inputs, out);
    }

    public abstract double[] normalizeOutputVector(double[] outputVector);

    public abstract double[] denormalizeOutputVector(double[] outputVector);

    public abstract String[] normalizeInputs(String[] inputEquation);

    public abstract double[] normalizeInputVector(double[] inputVector);

    public abstract double denormalizeTarget(double output);

    public abstract double normalizeTarget(double output);
}
