package configuration.models.single;

import game.models.single.SigmoidNormModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sigmoid transfer function
 */
public class SigmoidNormModelConfig extends ModelSingleConfigBase {

    public SigmoidNormModelConfig() {
        super();
        setClassRef(SigmoidNormModel.class);
    }
}
