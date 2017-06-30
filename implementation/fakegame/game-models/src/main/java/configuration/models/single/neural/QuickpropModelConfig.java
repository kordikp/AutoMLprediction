package configuration.models.single.neural;

import game.models.single.neural.QuickpropModel;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.CheckBox;

/**
 * Configuration for Quickprop regression model.
 */
public class QuickpropModelConfig extends ModelNeuralConfigBase {

    @Property(name = "max growth factor", description = "maximum growth factor")
    protected double maxGrowthFactor;
    @Property(name = "epsilon", description = "epsilon")
    protected double epsilon;
    @Property(name = "Split Epsilon", description = "If checked then split epsilon is used.")
    @CheckBox
    protected boolean splitEpsilon;

    public QuickpropModelConfig() {
        super();
        epsilon = 0.0007;
        maxGrowthFactor = 2.0;
        splitEpsilon = false;
        firstLayerNeurons = 5;
        secondLayerNeurons = 0;
        trainingCycles = 600;
        setClassRef(QuickpropModel.class);
    }

    @Override
    protected String variablesToString() {
        String actFnc = activationFunction.getEnabledElements(String.class)[0].toString();
        return "(epsilon=" + epsilon + "," +
                "maxGrowthFactor=" + maxGrowthFactor + "," +
                "splitE=" + splitEpsilon + "," +
                "1LNeurons=" + firstLayerNeurons + "," +
                "2LNeurons=" + secondLayerNeurons + "," +
                "trCycles=" + trainingCycles + "," +
                "actFnc=" + actFnc + ")";
    }

    public double getMaxGrowthFactor() {
        return maxGrowthFactor;
    }

    public void setMaxGrowthFactor(double maxGrowthFactor) {
        this.maxGrowthFactor = maxGrowthFactor;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public boolean getSplitEpsilon() {
        return splitEpsilon;
    }

    public void setSplitEpsilon(boolean splitEpsilon) {
        this.splitEpsilon = splitEpsilon;
    }
}
