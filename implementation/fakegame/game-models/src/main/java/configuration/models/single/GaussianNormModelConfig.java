package configuration.models.single;

import game.models.single.GaussianNormModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sine transfer function
 */
public class GaussianNormModelConfig extends ModelSingleConfigBase {

    public GaussianNormModelConfig() {
        super();
        setClassRef(GaussianNormModel.class);
    }
}
