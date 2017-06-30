package configuration.models.single;

import game.models.single.SineModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sine transfer function
 */
public class SineModelConfig extends ModelSingleConfigBase {

    public SineModelConfig() {
        super();
        setClassRef(SineModel.class);
    }
}
