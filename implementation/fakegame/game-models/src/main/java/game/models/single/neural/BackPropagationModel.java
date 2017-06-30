package game.models.single.neural;

import configuration.models.ModelConfig;
import configuration.models.single.neural.BackPropagationModelConfig;
import game.classifiers.neural.BackPropagation;
import game.classifiers.neural.NeuralNetwork;
import game.classifiers.neural.SlopeCalcFunctionBackProp;

import java.io.Serializable;

/**
 * Backpropagation model
 */
public class BackPropagationModel extends NeuralModel implements Serializable{

    protected double learningRate;
    protected double momentum;

    @Override
    public Class getConfigClass() {
        return BackPropagationModelConfig.class;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        BackPropagationModelConfig cf = (BackPropagationModelConfig) cfg;
        learningRate = cf.getLearningRate();
        momentum = cf.getMomentum();
        firstLayerNeurons = cf.getFirstLayerNeurons();
        secondLayerNeurons = cf.getSecondLayerNeurons();
        trainingCycles = cf.getTrainingCycles();
        activationFunctionName = cf.getActivationFunction().getEnabledElements(String.class)[0];
    }

    @Override
    public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += inputEquation[i] + ((i < inputsNumber - 1) ? ", " : "");
        }
        return "Backpropagation(" + s + ")";
    }

    @Override
    public void learn() {
        super.initNeuralNetwork();
        slopeCalcFunction = new SlopeCalcFunctionBackProp();
        learningAlgorithm = new BackPropagation(network, learningRate, momentum);
        super.train();
    }
}

