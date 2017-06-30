package configuration.models.ensemble;

import game.models.ensemble.ModelBoostingR2;
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
@Component(name = "BoostingR2ModelConfig", description = "Configuration of the Boosting ensembling strategy")
public class BoostingR2ModelConfig extends ModelEnsembleConfigBase {
    @Property(name = "Next models specialization", description = "Determines how much will be next model specialized " +
            "on wrong responses by previous model")
    protected double modelsSpecialization;

    public BoostingR2ModelConfig() {
        super();
        modelsSpecialization = 1;
        classRef = ModelBoostingR2.class;
    }

    protected String variablesToString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "(spec=" + df.format(modelsSpecialization) + ")";
    }

    public double getModelsSpecialization() {
        return modelsSpecialization;
    }

    public void setModelsSpecialization(double modelsSpecialization) {
        this.modelsSpecialization = modelsSpecialization;
    }


}