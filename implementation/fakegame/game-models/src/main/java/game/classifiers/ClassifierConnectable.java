package game.classifiers;

import configuration.CfgTemplate;
import game.data.DataNormalizer;
import game.data.Mode;
import game.data.OutputProducer;

import java.util.Vector;

/**
 * Allows to connect classifiers into a hierarchical structure and to input data features
 */
public interface ClassifierConnectable extends Classifier {
    /**
     * Initialises embedded model
     *
     * @param modelConfig configuration of the model
     * @param inputs      vector of inputs
     */
    public void init(CfgTemplate modelConfig, Vector<OutputProducer> inputs, DataNormalizer normalizer);

    /**
     * Provides list of inputs to the model
     *
     * @return vector of inputs (models of features)
     */
    public Vector<OutputProducer> getInputs();

    /**
     * Connects model to other models or to an input features
     *
     * @param inputs vector of inputs
     */
    public void connectTo(Vector<OutputProducer> inputs);

    /**
     * Connects single input
     *
     * @param index index of input
     * @param input input to model of input feature
     */
    public void setInput(int index, OutputProducer input);

    /**
     * Deletes vector of inputs
     */
    public void disconnect();

    /**
     * Deleteds particular input
     *
     * @param index input to disconnect
     */
    public void disconnectInput(int index);

    /**
     * Model can work in passive and active mode - in passive mode, last (precomputed) output is returned by getOutput()
     *
     * @return mode
     */
    public Mode getMode();

    /**
     * You can set mode to PASSIVE or ACTIVE
     *
     * @param mode mode to set
     */
    public void setMode(Mode mode);

    /**
     * Precomputes output and goes to passive mode
     */
    public void toPassiveMode();
}