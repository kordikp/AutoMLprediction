package configuration.models.single;

import game.models.single.LinearModel;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.CheckBox;


/**
 * Configuration bean of the model with linear transfer function
 */
public class LinearModelConfig extends ModelSingleConfigBase {

    @Property(name = "Use default optimization when LMS fails", description = "When enabled, default optimization methods are used in case of singular matrix in LMS")
    protected boolean retrainWhenLmsFails;

    public boolean getRetrainWhenLmsFails() {
        return retrainWhenLmsFails;
    }

    public void setRetrainWhenLmsFails(boolean retrainWhenLmsFails) {
        this.retrainWhenLmsFails = retrainWhenLmsFails;
    }

    public LinearModelConfig() {
        super();
        setClassRef(LinearModel.class);
    }

}
