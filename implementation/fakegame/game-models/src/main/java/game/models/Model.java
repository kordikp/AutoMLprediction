package game.models;

import configuration.models.ModelConfig;
import game.configuration.Configurable;

/**
 * Model has just single continuous output on the contrary to game.classifiers package
 * <p>
 * kordikp
 */
public interface Model extends Configurable {
    /**
     * Returns name of the model, it doesnt need to be unique
     *
     * @return Name of the model
     */
    public String getName();

    public void setName(String name);

    /**
     * Gets and sets name of the trainer used for actual model
     *
     * @return Name of the training algorithm
     */
    public String getTrainedBy();

    public void setTrainedBy(String trainerName);

    /**
     * Computes response of the model to the particular input vector
     *
     * @param input_vector Specify inputs to the model
     * @return output of model based on the input vector
     */
    public double getOutput(double[] input_vector);

    /**
     * This function returns number of output that is modelled
     *
     * @return index of output variable that is modelled
     */
    public int getTargetVariable();

    /**
     * In ensemble it also sets targetVariable to given value of all ensembled models.
     *
     * @param targetVariable index of output variable that is modelled.
     */
    public void setTargetVariable(int targetVariable);

    /**
     * Composition Info serialized into a String
     *
     * @return Model Info
     */
    public String toString();

    /**
     * Math equation derived from the model
     *
     * @param inputEquation names of input variables or equations of input models
     * @return String containing the math equation
     */
    public String toEquation(String[] inputEquation);

    /**
     * @return Returns configuration for current model.
     */
    public ModelConfig getConfig();

    @Deprecated
    public double getNormalizedOutput(double[] inputVector);

}
