package game.classifiers;

import configuration.classifiers.ClassifierConfig;

import game.configuration.Configurable;

/**
 * Interface for classifiers
 * Author: cernyjn
 */
public interface Classifier extends Configurable {
    /**
     * Initialises a classifier
     *
     * @param cfg configuration template
     */
    public void init(ClassifierConfig cfg);

    /**
     * Learn all models contained in classifier that are not learned.
     */
    public void learn();

    /**
     * Learn all models contained in classifier.
     */
    public void relearn();

    /**
     * @param input_vector Input data vector.
     * @return Returns outputs normalized to probabilities (output variables are from interval <0,1> and sum of all
     * output variables = 1)
     */
    public double[] getOutputProbabilities(double[] input_vector);

    /**
     * @param input_vector Data vector to be classified.
     * @return Returns attribute index of model which has highest output value.
     */
    public int getOutput(double[] input_vector);

    /**
     * @return maximum number of vectors that can be used to train model
     */
    public int getMaxLearningVectors();

    /**
     * Needed for setting variables via anotations. Uses resizeDataFields.
     *
     * @param maxVectors new maximum of learning vectors.
     */
    public void setMaxLearningVectors(int maxVectors);

    /**
     * Adds particular data vector to the learnig data set of the model - allows you to add data indepentently from GlobalData
     *
     * @param input  input vector
     * @param output target variable
     */
    public void storeLearningVector(double[] input, double[] output);

    /**
     * @return true if model is learned.
     */
    public boolean isLearned();

    /**
     * Math equation derived from the classifier
     *
     * @param inputEquation inputs provided by connectable classifier
     * @return String containing the math equation
     */
    public String toEquation(String[] inputEquation);

    /**
     * @param inputEquation provided by connectable classifier
     * @return Returns array of equations for all output variables.
     * Functions used in equations
     * firstTrue(array,condition,false_expression)
     * array - array of equations.
     * condition - condition that is checked for every equation and if it is true returns equation value.
     * false_expression - expression that is returned if none of the equations satisfies the condition.
     */
    public String[] getEquations(String[] inputEquation);

    /**
     * Returns name of the model, it doesnt need to be unique
     *
     * @return Name of the model
     */
    public String getName();

    public void setName(String name);

    /**
     * Deletes all learning data from current classifiers and all subclassifiers
     */
    public void deleteLearningVectors();

    /**
     * Resets internal pointer for learning vectors to 0, ie erases all learning data, but leaves memory allocated.
     */
    public void resetLearningData();

    /**
     * @return Returns configuration for current classifier.
     */
    public ClassifierConfig getConfig();

    public void setInputsNumber(int inputs);

    public void setOutputsNumber(int outs);

    public int getInputsNumber();

    public int getOutputsNumber();

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
     * Returns all output vectors being stored in learning data.
     * The resulting array is formed in same way as the array got by {@link game.data.GameData.getOutputAttrs()},
     * which means, that while addressing a value, the top-level array index is a number of output,
     * the second-level array index is a number of data instance.
     * The values are ordered to match instance ordering in {@link #getLearningInputVectors()}.
     *
     * @return array with outputs of all learning vectors
     */
    public double[][] getLearningOutputVectors();

}
