package configuration.models.single;


import game.models.single.ExpModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with sigmoid transfer function
 */
public class ExpModelConfig extends ModelSingleConfigBase {

    public ExpModelConfig() {
        super();
        setClassRef(ExpModel.class);
    }

}
