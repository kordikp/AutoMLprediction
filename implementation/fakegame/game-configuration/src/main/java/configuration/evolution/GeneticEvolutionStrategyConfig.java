package configuration.evolution;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * Created by IntelliJ IDEA.
 * User: kordikp
 * Date: 7.11.2009
 * Time: 13:36:31
 * To change this template use File | Settings | File Templates.
 */
@Component(name = "GeneticEvolutionStrategyConfig", description = "Configuration of the genetic optimization of models")
public class GeneticEvolutionStrategyConfig extends BaseEvolutionStrategyConfig {
    @Property(name = "Mutation Rate", description = "Mutation rate in the deterministic crowding algorithm")
    private double mutationRate;

    public GeneticEvolutionStrategyConfig() {
        super();
        mutationRate = 0.1;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }
}
