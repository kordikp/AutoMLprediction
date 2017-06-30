package configuration.models.single;

import game.models.single.SineNormModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sine transfer function (normailzed inputs and output
 */
public class SineNormModelConfig extends ModelSingleConfigBase {
    
    public SineNormModelConfig() {
        super();
        setClassRef(SineNormModel.class);
    }
}
