package configuration.models.ensemble;

import game.models.ensemble.ModelStacking;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the stacking ensemble of models
 */
@Component(name = "StackingModelConfig", description = "Configuration of the Stacking ensembling strategy")
public class StackingModelConfig extends ModelEnsembleConfigBase {
    public StackingModelConfig() {
        super();
        classRef = ModelStacking.class;
    }
}