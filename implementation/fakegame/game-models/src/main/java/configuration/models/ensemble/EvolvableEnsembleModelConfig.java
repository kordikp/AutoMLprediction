package configuration.models.ensemble;

import game.models.ensemble.ModelEvolvableEnsemble;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * Configures the bagging ensembling strategy. There are more definitions how the configuration can look like:
 * when baseModelsDef is
 * - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
 * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
 * - EVOLVED: the same as random, just allowed models are evolved by genetic algorithm to find best ensemble
 * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
 * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
 */
@Component(name = "EvolvableEnsembleModelConfig", description = "Configuration of the abstract method for optimization of models with genome represenation")
public class EvolvableEnsembleModelConfig extends ModelEnsembleConfigBase {
    @Property(name = "Number of generations", description = "Epochs of the algorithm")
    protected int generations = 50;

    @Property(name = "Learning to Validation data ratio", description = "Percent of data vectors used as learning set")
    protected int learnValidRatio = 50;

    @Property(name = "Genotypic distance enabled", description = "Hamming distance of genomes added to distance")
    boolean genoDistanceEnabled = true;
    @Property(name = "Negative correlation distance", description = "Negative correlation of outputs added to distance")
    boolean correlationDistanceEnabled = true;
    @Property(name = "Outputs distance", description = "Outputs difference added to distance")
    boolean outputsDistanceEnabled = true;

    //todo gui - set evolution strategy
    protected Class evolutionStrategyClass;

    @Property(name = "Evolution strategy config", description = "Configure evolution strategy")
    Object evolutionStrategyConfig;

    public EvolvableEnsembleModelConfig() {
        classRef = ModelEvolvableEnsemble.class;
    }

    public boolean isGenoDistanceEnabled() {
        return genoDistanceEnabled;
    }

    public void setGenoDistanceEnabled(boolean genoDistanceEnabled) {
        this.genoDistanceEnabled = genoDistanceEnabled;
    }

    public boolean isCorrelationDistanceEnabled() {
        return correlationDistanceEnabled;
    }

    public void setCorrelationDistanceEnabled(boolean correlationDistanceEnabled) {
        this.correlationDistanceEnabled = correlationDistanceEnabled;
    }

    public boolean isOutputsDistanceEnabled() {
        return outputsDistanceEnabled;
    }

    public void setOutputsDistanceEnabled(boolean outputsDistanceEnabled) {
        this.outputsDistanceEnabled = outputsDistanceEnabled;
    }

    public int getGenerations() {
        return generations;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }

    public int getLearnValidRatio() {
        return learnValidRatio;
    }

    public void setLearnValidRatio(int learnValidRatio) {
        this.learnValidRatio = learnValidRatio;
    }

    public Object getEvolutionStrategyConfig() {
        return evolutionStrategyConfig;
    }

    public void setEvolutionStrategyConfig(Object evolutionStrategyConfig) {
        this.evolutionStrategyConfig = evolutionStrategyConfig;
    }

    public Class getEvolutionStrategyClass() {
        return evolutionStrategyClass;
    }

    public void setEvolutionStrategy(Class evolutionStrategyClass) {
        this.evolutionStrategyClass = evolutionStrategyClass;
    }
}
