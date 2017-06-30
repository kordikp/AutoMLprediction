package configuration.models.ensemble;

/**
 * Various forms of base models definition
 * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
 * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
 * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
 * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
 */
public enum BaseModelsDefinition {
    PREDEFINED, RANDOM, UNIFORM, UNIFORM_RANDOM
}
