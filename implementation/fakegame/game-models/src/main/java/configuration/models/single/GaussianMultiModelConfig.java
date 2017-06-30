package configuration.models.single;


import game.models.single.GaussianMultiModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sine transfer function
 */
public class GaussianMultiModelConfig extends ModelSingleConfigBase {

    public GaussianMultiModelConfig() {
        super();
        setClassRef(GaussianMultiModel.class);
    }
}
