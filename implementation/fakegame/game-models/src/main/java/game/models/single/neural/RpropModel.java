package game.models.single.neural;

import configuration.models.ModelConfig;
import configuration.models.single.neural.RpropModelConfig;
import game.classifiers.neural.RProp;
import game.classifiers.neural.SlopeCalcFunctionRprop;

import java.io.Serializable;

/**
 * Rprop model
 */
public class RpropModel extends NeuralModel implements Serializable {

    protected double etaMinus;
    protected double etaPlus;

    @Override
    public Class getConfigClass() {
        return RpropModelConfig.class;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        RpropModelConfig cf = (RpropModelConfig) cfg;
        etaMinus = cf.getEtaMinus();
        etaPlus = cf.getEtaPlus();
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
        return "Rprop(" + s + ")";
    }

    @Override
    public void learn() {
        super.initNeuralNetwork();
        slopeCalcFunction = new SlopeCalcFunctionRprop();
        learningAlgorithm = new RProp(network, etaMinus, etaPlus);
        super.train();
    }
}
