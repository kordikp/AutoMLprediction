package configuration.models.ensemble;

import game.models.ensemble.ModelBagging;
import org.ytoh.configurations.annotations.Component;

/**
 * Configures the bagging ensembling strategy. There are more definitions how the configuration can look like:
 * when baseModelsDef is
 * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
 * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
 * - EVOLVED: the same as random, just allowed models are evolved by genetic algorithm to find best ensemble
 * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
 * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
 */
@Component(name = "BaggingModelConfig", description = "Configuration of the Bagging ensembling strategy")
public class BaggingModelConfig extends ModelEnsembleConfigBase {

    public BaggingModelConfig() {
        classRef = ModelBagging.class;
    }
}