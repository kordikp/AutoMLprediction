package game.models.single.neural;

import game.models.ModelLearnableBase;
import game.classifiers.neural.*;

import java.io.Serializable;

/**
 * Abstract class for neural models from neural library.
 */
abstract public class NeuralModel extends ModelLearnableBase implements Serializable{

    protected NeuralNetwork network;
    transient protected TrainingSet trainingSet;
    transient protected ILearningAlgorithm learningAlgorithm;
    protected IActivationFunction activationFunction;
    transient protected ISlopeCalcFunction slopeCalcFunction;

    protected int firstLayerNeurons;
    protected int secondLayerNeurons;
    protected int trainingCycles;
    protected double acceptableError;
    protected String activationFunctionName;

    @Override
    public double getOutput(double[] input_vector) {
        TrainingPattern pattern = new TrainingPattern(input_vector, new double[1]);
        double[] outputs = new double[1];
        try {
            outputs = this.network.extractOutput(pattern);
            return outputs[0];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return outputs[0];
    }

    public double getOutputTo(int inputVectorIndex) {
        return getOutput(inputVect[inputVectorIndex]);
    }

    protected void train() {
        SlopeCalcParams info = new SlopeCalcParams();
        info.mode = TrainMode.minimize;
        info.neuralNetwork = network;
        info.synapsesToTrain = network.synapses();

        trainingSet = new TrainingSet();
        for (int i = 0; i < learning_vectors; i++) {
            double[] criteria = new double[1];
            criteria[0] = target[i];
            TrainingPattern pattern = new TrainingPattern(inputVect[i], criteria);
            trainingSet.addTrainingPattern(pattern);
        }

        try {
            for (int i = 0; i < trainingCycles; i++) {
                double error = network.calculateSquaredError(trainingSet);
                if (error < acceptableError) {
                    break;
                } else {
                    learningAlgorithm.train(trainingSet, info, slopeCalcFunction);
                    // network.printError(trainingSet);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        learned = true;
    }

    protected void initNeuralNetwork() {
        try {
            setActivationFunction();
            checkNetIntegrity();
            network = new NeuralNetwork(inputsNumber, 1, 1, activationFunction, false);
            if (firstLayerNeurons == 0 && secondLayerNeurons == 0) {
                network.fullyConnectLayers(network.layers().get(0), network.layers().get(1), true);
            } else if (secondLayerNeurons == 0) {
                NeuronLayer firstHiddenLayer = network.createLayer(firstLayerNeurons, LayerType.hidden, network.neuronId, 0, activationFunction);
                network.addHiddenLayer(firstHiddenLayer, 1);
                network.fullyConnectLayers(network.layers().get(0), network.layers().get(1), true);
                network.fullyConnectLayers(network.layers().get(1), network.layers().get(2), true);
            } else {
                NeuronLayer firstHiddenLayer = network.createLayer(firstLayerNeurons, LayerType.hidden, network.neuronId, 0, activationFunction);
                NeuronLayer secondHiddenLayer = network.createLayer(secondLayerNeurons, LayerType.hidden, network.neuronId, 0, activationFunction);
                network.addHiddenLayer(firstHiddenLayer, 1);
                network.addHiddenLayer(secondHiddenLayer, 2);
                network.fullyConnectLayers(network.layers().get(0), network.layers().get(1), true);
                network.fullyConnectLayers(network.layers().get(1), network.layers().get(2), true);
                network.fullyConnectLayers(network.layers().get(2), network.layers().get(3), true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkNetIntegrity() {
        if (firstLayerNeurons == 0 && secondLayerNeurons != 0) {
            firstLayerNeurons = secondLayerNeurons;
            secondLayerNeurons = 0;
        }
    }

    protected void setActivationFunction() {
        if (activationFunctionName.equals("sigmoid")) {
            activationFunction = new ActivationFunctionSigmoid();
        } else if (activationFunctionName.equals("sigmoid_offset")) {
            activationFunction = new ActivationFunctionSigmoidFahlmanOffset();
        } else if (activationFunctionName.equals("symmetric_sigmoid")) {
            activationFunction = new ActivationFunctionSymmetricSigmoid();
        }
    }

}