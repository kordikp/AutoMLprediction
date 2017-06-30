package game.models;

import game.data.GlobalData;
import configuration.models.ModelConfig;

/**
 * Abstract class for commonly used methods in models.
 * Author:cernyjn
 */
public abstract class ModelLearnableBase implements ModelLearnable {
    protected transient double[][] inputVect;
    protected transient double[] target;
    protected int learning_vectors;
    protected int inputsNumber;
    protected String trainedBy;
    protected int targetVariable;
    protected boolean learned;
    protected String name;
    protected int maxLearningVectors;

    public void init(ModelConfig cfg) {
        maxLearningVectors = cfg.getMaxLearningVectors();
        targetVariable = cfg.getTargetVariable();
        name = cfg.getName();
        //initialize variables
        learning_vectors = 0;
        learned = false;
    }

    public void storeLearningVector(double[] input, double output) {
        if (learning_vectors == 0) {
            setInputsNumber(input.length);
        }
        target[learning_vectors] = output;
        System.arraycopy(input, 0, inputVect[learning_vectors], 0, inputsNumber);

        learning_vectors++;
    }

    /**
     * Performs initialization of all internal structures depending on number of inputs.
     *
     * @param inputs Number of inputs for given model.
     */
    public void setInputsNumber(int inputs) {
        inputsNumber = inputs;
        inputVect = new double[maxLearningVectors][inputsNumber];
        target = new double[maxLearningVectors];
    }

    public void setMaxLearningVectors(int maxVectors) {
        if (maxLearningVectors == -1) maxLearningVectors = maxVectors;
    }

    public ModelConfig getConfig() {
        ModelConfig cfg = null;
        try {
            cfg = (ModelConfig) getConfigClass().newInstance();
            cfg.setClassRef(this.getClass());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTrainedBy() {
        return trainedBy;
    }

    public int getMaxLearningVectors() {
        return maxLearningVectors;
    }

    public void setTrainedBy(String trainerName) {
        trainedBy = trainerName;
    }

    public int getTargetVariable() {
        return targetVariable;
    }

    public void setTargetVariable(int targetVariable) {
        this.targetVariable = targetVariable;
    }

    public boolean isLearned() {
        return learned;
    }

    public void deleteLearningVectors() {
        learning_vectors = 0;
        inputVect = new double[0][0];
        target = new double[0];
    }

    public void resetLearningData() {
        learning_vectors = 0;
    }

    @Deprecated
    public double getNormalizedOutput(double[] normalized_input_vector) {
        double[] vector = GlobalData.getInstance().denormalizeInputVector(normalized_input_vector);
        //transform output back to <0,1> interval
        return GlobalData.getInstance().getStandardOutput(targetVariable, getOutput(vector));
    }

    public int getInputsNumber() {
        return inputsNumber;
    }

    public double[][] getLearningInputVectors() {
        if (inputVect.length < 1) return new double[0][0];
        double[][] res = new double[inputVect[0].length][inputVect.length];
        for (int i = 0; i < inputVect.length; i++) {
            for (int j = 0; j < inputVect[0].length; j++) {
                res[j][i] = inputVect[i][j];
            }
        }
        return res;
    }

    public double[] getLearningOutputVectors() {
        return target;
    }
}
