package game.data;

/**
 * This class helps to normalize data internally for ConnectableClassifiers and ConnectableModels
 */
public class MinMaxDataNormalizer extends DataNormalizer {
    protected double[] imins;
    protected double[] imaxs;
    protected double[] omins;
    protected double[] omaxs;

    @Override
    public void init(double[][] inputs, double[][] outputs) {
        super.init(inputs, outputs);
        countRanges();
    }

    private void countRanges() {
        int size = inputs.length;
        int inps = inputs[0].length;
        int outs = outputs[0].length;
        imins = new double[inps];
        imaxs = new double[inps];
        omins = new double[outs];
        omaxs = new double[outs];
        for (int i = 0; i < inps; i++) {
            imins[i] = Double.MAX_VALUE;
            imaxs[i] = -1 * Double.MAX_VALUE;
        }
        for (int i = 0; i < outs; i++) {
            omins[i] = Double.MAX_VALUE;
            omaxs[i] = -1 * Double.MAX_VALUE;
        }
        for (int i = 0; i < size; i++)
            for (int j = 0; j < inps; j++) {
                if (inputs[i][j] < imins[j]) imins[j] = inputs[i][j];
                if (inputs[i][j] > imaxs[j]) imaxs[j] = inputs[i][j];
            }
        for (int i = 0; i < size; i++)
            for (int j = 0; j < outs; j++) {
                if (outputs[i][j] < omins[j]) omins[j] = outputs[i][j];
                if (outputs[i][j] > omaxs[j]) omaxs[j] = outputs[i][j];
            }
        for (int i = 0; i < inps; i++) {
            if (imins[i] == imaxs[i]) imaxs[i]++;   // range 0 not allowed
        }
        for (int i = 0; i < outs; i++) {
            if (omins[i] == omaxs[i]) omaxs[i]++;
        }
    }

    public String[] normalizeInputs(String[] inputEquation) {
        int inps = inputEquation.length;
        String[] ie = new String[inps];
        for (int i = 0; i < inps; i++) {
            ie[i] = "(" + inputEquation[i] + "-" + imins[i] + ")/" + (imaxs[i] - imins[i]);
        }
        return ie;
    }

    public double[] normalizeInputVector(double[] inputVector) {
        int inps = inputVector.length;
        double[] iv = new double[inps];
        for (int i = 0; i < inps; i++)
            iv[i] = (inputVector[i] - imins[i]) / (imaxs[i] - imins[i]);
        return iv;
    }

    public double[] normalizeOutputVector(double[] outputVector) {
        int outs = outputVector.length;
        double[] ov = new double[outs];
        for (int i = 0; i < outs; i++)
            ov[i] = (outputVector[i] - omins[i]) / (omaxs[i] - omins[i]);
        return ov;
    }

    public double[] denormalizeOutputVector(double[] outputVector) {
        int outs = outputVector.length;
        double[] ov = new double[outs];
        for (int i = 0; i < outs; i++)
            ov[i] = outputVector[i] * (omaxs[i] - omins[i]) + omins[i];
        return ov;
    }

    public double denormalizeTarget(double output) {
        return omins[0] + (omaxs[0] - omins[0]) * output;
    }

    public double normalizeTarget(double output) {
        return (output - omins[0]) / (omaxs[0] - omins[0]);
    }
}