package configuration.models.single;

import game.models.single.SigmoidModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sigmoid transfer function
 */
public class SigmoidModelConfig extends ModelSingleConfigBase {

    public SigmoidModelConfig() {
        super();
        setClassRef(SigmoidModel.class);
    }

}
