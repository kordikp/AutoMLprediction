package configuration.models.single;

import game.models.single.GaussianModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sine transfer function
 */
public class GaussianModelConfig extends ModelSingleConfigBase {

    public GaussianModelConfig() {
        super();
        setClassRef(GaussianModel.class);
    }
}
