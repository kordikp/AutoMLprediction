package configuration.models.single.neural;

import game.models.single.neural.RpropModel;
import org.ytoh.configurations.annotations.*;

/**
 * Configuration for Rprop regression model.
 */
public class RpropModelConfig extends ModelNeuralConfigBase {

    @Property(name = "eta minus", description = "eta minus")
    protected double etaMinus;
    @Property(name = "eta plus", description = "eta plus")
    protected double etaPlus;

    public RpropModelConfig() {
        super();
        etaMinus = 0.5;
        etaPlus = 1.2;
        firstLayerNeurons = 5;
        secondLayerNeurons = 0;
        trainingCycles = 600;
        setClassRef(RpropModel.class);
    }

    @Override
    protected String variablesToString() {
        String actFnc = activationFunction.getEnabledElements(String.class)[0].toString();
        return "(etaMinus=" + etaMinus + "," +
                "etaPlus=" + etaPlus + "," +
                "1LNeurons=" + firstLayerNeurons + "," +
                "2LNeurons=" + secondLayerNeurons + "," +
                "trCycles=" + trainingCycles + "," +
                "actFnc=" + actFnc + ")";
    }

    public double getEtaMinus() {
        return etaMinus;
    }

    public void setEtaMinus(double etaMinus) {
        this.etaMinus = etaMinus;
    }

    public double getEtaPlus() {
        return etaPlus;
    }

    public void setEtaPlus(double etaPlus) {
        this.etaPlus = etaPlus;
    }
}
