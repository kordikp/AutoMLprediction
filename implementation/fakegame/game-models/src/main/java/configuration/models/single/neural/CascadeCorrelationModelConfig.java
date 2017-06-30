package configuration.models.single.neural;

import configuration.models.ModelConfigBase;
import game.models.single.neural.CascadeCorrelationModel;
import org.ytoh.configurations.annotations.Property;

import org.ytoh.configurations.annotations.SelectionSet;
import org.ytoh.configurations.ui.SelectionSetModel;

/**
 * Configuration for cascade correlation regression model.
 */
public class CascadeCorrelationModelConfig extends ModelConfigBase {

    @Property(name = "Acceptable error", description = "Learning terminates when training error gets below this value.")
    protected double acceptableError;
    @Property(name = "Maximum Layers", description = "Maximum number of hidden layers.")
    protected int maxLayersNumber;
    @Property(name = "Candidate number", description = "Number of candidate neurons.")
    protected int candNumber;
    @Property(name = "Used algorithm", description = "Algorithm used to train network.")
    @SelectionSet(key = "usedAlgName", type = SelectionSetModel.class)
    protected SelectionSetModel<String> usedAlg;
    @Property(name = "Activation function", description = "Activation function")
    @SelectionSet(key = "activationFunctionName", type = SelectionSetModel.class)
    protected SelectionSetModel<String> activationFunction;


    public CascadeCorrelationModelConfig() {
        super();
        acceptableError = 0.001;
        maxLayersNumber = 5;
        candNumber = 1;

        String[] algTypes = new String[]{
                "Quickprop",
                "Rprop",
        };
        usedAlg = new SelectionSetModel<String>(algTypes);
        usedAlg.disableAllElements();
        usedAlg.enableElement(1);

        String[] functions = new String[]{
                "sigmoid",
                "sigmoid_offset",
                "symmetric_sigmoid"
        };
        activationFunction = new SelectionSetModel<String>(functions);
        activationFunction.disableAllElements();
        activationFunction.enableElement(1);

        setClassRef(CascadeCorrelationModel.class);
    }

    @Override
    protected String variablesToString() {
        String actFnc = activationFunction.getEnabledElements(String.class)[0].toString();
        String usAlg = usedAlg.getEnabledElements(String.class)[0].toString();
        return "(acceptableE=" + acceptableError + "," +
                "maxLayersNumber=" + maxLayersNumber + "," +
                "candNumber=" + candNumber + "," +
                "learningAlg=" + usAlg + "," +
                "actFnc=" + actFnc + ")";
    }

    public double getAcceptableError() {
        return acceptableError;
    }

    public void setAcceptableError(double acceptableError) {
        this.acceptableError = acceptableError;
    }

    public int getMaxLayersNumber() {
        return maxLayersNumber;
    }

    public void setMaxLayersNumber(int maxLayersNumber) {
        this.maxLayersNumber = maxLayersNumber;
    }

    public int getCandNumber() {
        return candNumber;
    }

    public void setCandNumber(int candNumber) {
        this.candNumber = candNumber;
    }

    public SelectionSetModel<String> getActivationFunction() {
        return activationFunction;
    }

    public void setActivationFunction(SelectionSetModel<String> activationFunction) {
        this.activationFunction = activationFunction;
    }

    public SelectionSetModel<String> getUsedAlg() {
        return usedAlg;
    }

    public void setUsedAlg(SelectionSetModel<String> usedAlg) {
        this.usedAlg = usedAlg;
    }
}
