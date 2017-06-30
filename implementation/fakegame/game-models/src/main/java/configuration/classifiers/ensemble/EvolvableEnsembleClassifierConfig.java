package configuration.classifiers.ensemble;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * Configures an evolvable classifier ensemble
 */
@Component(name = "EvolvableEnsembleClassifierConfig", description = "Configuration of the abstract method for optimization of models with genome represenation")
public class EvolvableEnsembleClassifierConfig extends EnsembleClassifierConfigBase {
    @Property(name = "Number of generations", description = "Epochs of the algorithm")
    protected int generations = 50;

    @Property(name = "Learning to Validation data ratio", description = "Percent of data vectors used as learning set")
    protected int learnValidRatio = 50;

    //todo gui - set evolution strategy
    protected Class evolutionStrategyClass;

    @Property(name = "Evolution strategy config", description = "Configure evolution strategy")
    Object evolutionStrategyConfig;

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