package game.models;

import configuration.models.ModelConfig;


/**
 * This interface represents models that are capable of learning.
 */
public interface ModelLearnable extends Model {

    /**
     * This function is used to intialise the model acording to the template configuration bean
     *
     * @param cfg
     */
    public void init(ModelConfig cfg);

    /**
     * This member fuction runs the learnig algorithm of the model
     */
    public void learn();

    /**
     * @return maximum number of vectors that can be used to train model
     */
    public int getMaxLearningVectors();

    /**
     * @param maxVectors Set maximum number of vectors that can be used for learning.
     */
    public void setMaxLearningVectors(int maxVectors);


    /**
     * Adds particular data vector to the learnig data set of the model - allows you to add data indepentently from GlobalData
     *
     * @param input  input vector
     * @param output target variable
     */
    public void storeLearningVector(double[] input, double output);

    /**
     * Deletes all learning vectors in this model and all submodels
     */
    public void deleteLearningVectors();

    /**
     * @return true if model is learned.
     */
    public boolean isLearned();

    /**
     * Resets internal pointer for learning vectors to 0, ie erases all learning data, but leaves memory allocated.
     */
    public void resetLearningData();

    /**
     * Performs initialization of all internal structures depending on number of inputs.
     *
     * @param inputs Number of inputs for given model.
     */
    public void setInputsNumber(int inputs);

    /**
     * Returns number of inputs.
     *
     * @return Number of inputs for given model.
     */
    public int getInputsNumber();

    /**
     * Returns all input vectors being stored in learning data.
     * The resulting array is formed in same way as the array got by {@link game.data.GameData.getInputVectors()},
     * which means, that while addressing a value, the top-level array index is a number of input,
     * the second-level array index is a number of data instance.
     *
     * @return array with inputs of all learning vectors
     */
    public double[][] getLearningInputVectors();

    /**
     * Returns target variable values of all data instances in learning data.
     * The values are ordered to match instance ordering in {@link #getLearningInputVectors()}.
     *
     * @return array with target variable values of all learning vectors
     */
    public double[] getLearningOutputVectors();

}
