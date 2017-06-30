package configuration.models.single.neural;

import configuration.models.ModelConfigBase;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.SelectionSet;
import org.ytoh.configurations.ui.CheckBox;
import org.ytoh.configurations.ui.SelectionSetModel;

/**
 * Abstract class for neural configurations.
 */
public abstract class ModelNeuralConfigBase extends ModelConfigBase {

    @Property(name = "1 hidden layer", description = "Number of neurons in first hidden layer.")
    protected int firstLayerNeurons;

    @Property(name = "2 hidden layer", description = "Number of neurons in second hidden layer.")
    protected int secondLayerNeurons;

    @Property(name = "training cycles", description = "Number of training cycles.")
    protected int trainingCycles;

    @Property(name = "acceptable error", description = "Learning terminates when training error gets below this value.")
    protected double acceptableError;

    @Property(name = "activation function", description = "Type of activation function.")
    @SelectionSet(key = "activationFunctionName", type = SelectionSetModel.class)
    protected SelectionSetModel<String> activationFunction;

    protected ModelNeuralConfigBase() {
        super();
        String[] functions = new String[]{
                "sigmoid",
                "sigmoid_offset",
                "symmetric_sigmoid"
        };
        activationFunction = new SelectionSetModel<String>(functions);
        activationFunction.disableAllElements();
        activationFunction.enableElement(0);
    }

    public int getFirstLayerNeurons() {
        return firstLayerNeurons;
    }

    public void setFirstLayerNeurons(int firstLayerNeurons) {
        this.firstLayerNeurons = firstLayerNeurons;
        checkNetIntegrity();
    }

    public int getSecondLayerNeurons() {
        return secondLayerNeurons;
    }

    public void setSecondLayerNeurons(int secondLayerNeurons) {
        this.secondLayerNeurons = secondLayerNeurons;
        checkNetIntegrity();
    }

    public int getTrainingCycles() {
        return trainingCycles;
    }

    public void setTrainingCycles(int trainingCycles) {
        this.trainingCycles = trainingCycles;
    }

    public double getAcceptableError() {
        return acceptableError;
    }

    public void setAcceptableError(double acceptableError) {
        this.acceptableError = acceptableError;
    }

    public SelectionSetModel<String> getActivationFunction() {
        return activationFunction;
    }

    public void setActivationFunction(SelectionSetModel<String> activationFunction) {
        this.activationFunction = activationFunction;
    }

    private void checkNetIntegrity() {
        if (firstLayerNeurons == 0 && secondLayerNeurons != 0) {
            firstLayerNeurons = secondLayerNeurons;
            secondLayerNeurons = 0;
            // System.out.println("check integrity");
        }
    }
}
