package game.models.single.neural;

import configuration.models.ModelConfig;
import configuration.models.single.neural.QuickpropModelConfig;
import game.classifiers.neural.QuickPropagation;
import game.classifiers.neural.SlopeCalcFunctionQuickProp;

import java.io.Serializable;

/**
 * Quickprop model
 */
public class QuickpropModel extends NeuralModel implements Serializable {

    protected double epsilon;
    protected double maxGrowthFactor;
    protected boolean splitEpsilon;

    @Override
    public Class getConfigClass() {
        return QuickpropModelConfig.class;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        QuickpropModelConfig cf = (QuickpropModelConfig) cfg;
        epsilon = cf.getEpsilon();
        maxGrowthFactor = cf.getMaxGrowthFactor();
        splitEpsilon = cf.getSplitEpsilon();
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
        return "Quickprop(" + s + ")";
    }

    @Override
    public void learn() {
        this.initNeuralNetwork();
        slopeCalcFunction = new SlopeCalcFunctionQuickProp();
        learningAlgorithm = new QuickPropagation(network, maxGrowthFactor, 0.0001, epsilon, splitEpsilon);
        this.train();
    }
}
