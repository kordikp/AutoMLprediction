package configuration.models.single.neural;

import game.models.single.neural.BackPropagationModel;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.SelectionSetModel;

/**
 * Configuration for Backpropagation regression model.
 */
public class BackPropagationModelConfig extends ModelNeuralConfigBase {

    @Property(name = "learning rate", description = "Learning rate determines by how much are the weights changed in each step.")
    protected double learningRate;
    @Property(name = "momentum", description = "Momentum adds fraction of previous weight update to current update.")
    protected double momentum;

    public BackPropagationModelConfig() {
        super();
        learningRate = 0.2;
        momentum = 0.3;
        firstLayerNeurons = 5;
        secondLayerNeurons = 0;
        trainingCycles = 600;

        //symmetric sigmoid doesn't work in BP
        String[] functions = new String[]{
                "sigmoid",
                "sigmoid_offset"
        };
        activationFunction = new SelectionSetModel<String>(functions);
        activationFunction.disableAllElements();
        activationFunction.enableElement(0);
        setClassRef(BackPropagationModel.class);
    }

    @Override
    protected String variablesToString() {
        String actFnc = activationFunction.getEnabledElements(String.class)[0].toString();
        return "(learningRate=" + learningRate + "," +
                "momentum=" + momentum + "," +
                "1LNeurons=" + firstLayerNeurons + "," +
                "2LNeurons=" + secondLayerNeurons + "," +
                "trCycles=" + trainingCycles + "," +
                "actFnc=" + actFnc + ")";
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double getMomentum() {
        return momentum;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }
}