package configuration.models.ensemble;

import game.models.ensemble.ModelGAME;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * Configures the GAME ensemblig strategy
 */
@Component(name = "GAMEEnsembleModelConfig", description = "Configuration of the GAME model ensembling strategy")
public class GAMEEnsembleModelConfig extends EvolvableEnsembleModelConfig {

    @Property(name = "Limit the number of inputs to neuron by index of its layer")
    private boolean increasingComplexity;

    public GAMEEnsembleModelConfig() {
        increasingComplexity = true;
        classRef = ModelGAME.class;
    }

    public boolean isIncreasingComplexity() {
        return increasingComplexity;
    }

    public int getMaxLayers() {
        return maxLayers;
    }

    public void setMaxLayers(int maxLayers) {
        this.maxLayers = maxLayers;
    }

    @Property(name = "Layers limit", description = "Maximum number of layers (0=no limit)")
    protected int maxLayers = 0;

    public void setIncreasingComplexity(boolean increasingComplexity) {
        this.increasingComplexity = increasingComplexity;
    }

}