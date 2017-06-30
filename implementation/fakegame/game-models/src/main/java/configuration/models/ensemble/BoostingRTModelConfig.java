package configuration.models.ensemble;

import game.models.ensemble.ModelBoostingRT;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import java.text.DecimalFormat;

/**
 * Configures the boosting ensembling strategy. There are more definitions how the configuration can look like:
 * when baseModelsDef is
 * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
 * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
 * - EVOLVED: the same as random, just allowed models are evolved by genetic algorithm to find best ensemble
 * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
 * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
 */
@Component(name = "BoostingRTModelConfig", description = "Configuration of the Boosting ensembling strategy")
public class BoostingRTModelConfig extends ModelEnsembleConfigBase {
    @Property(name = "Threshold", description = "Determines the threshold for dividing learning vectors into 2 groups - good response, bad response. " +
            "The weight of good response group will be reduced for next models during learning.")
    //todo:double values
    protected double threshold;

    public BoostingRTModelConfig() {
        super();
        threshold = 0.1;
        classRef = ModelBoostingRT.class;
    }

    protected String variablesToString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "(tr=" + df.format(threshold) + ")";
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}