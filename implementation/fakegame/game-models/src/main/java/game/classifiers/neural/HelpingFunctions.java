/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

/**
 * @author Administrator
 */
public class HelpingFunctions {
    /**
     * determines type of the neuron layer
     *
     * @param layerType
     * @return
     */
    static NeuronType determineNeuronType(LayerType layerType) {
        switch (layerType) {
            case input:
                return NeuronType.input;
            case hidden:
                return NeuronType.hidden;
            default:
                return NeuronType.output;
        }
    }

    static boolean isErrorAcceptable(TrainingSet trainingSet, NeuralNetwork network, double acceptableError) throws Exception {
        try {
            double currentError = network.calculateSquaredError(trainingSet);
            if (currentError <= acceptableError) return true;
            else return false;
        } catch (Exception ex) {
            throw new Exception("HelpingFunctions: isErrorAcceptable ->" + ex.getMessage());
        }
    }

    static boolean isErrorAcceptable(double currentError, double acceptableError) {
        if (currentError <= acceptableError) return true;
        else return false;

    }


}
