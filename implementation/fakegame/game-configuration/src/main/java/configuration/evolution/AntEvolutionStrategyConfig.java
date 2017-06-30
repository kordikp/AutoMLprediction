package configuration.evolution;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Range;

/**
 * Configuration of Ant Colony based model evolution strategy
 */
@Component(name = "AntEvolutionStrategyConfig", description = "Configuration of the ant optimization of models")
public class AntEvolutionStrategyConfig extends BaseEvolutionStrategyConfig {

    @Property(name = "Pheromone initial amount", description = "Initial amount of pheromone on all edges")
    @Range(from = .1, to = 1.0)
    private double pheromoneInit = 10.0;

    @Property(name = "Pheromone minimal amount", description = "Minimal amount of pheromone")
    @Range(from = .01, to = 1.0)
    private double pheromoneMin = 0.00000001;

    @Property(name = "Pheromone evaporation rate", description = "Multiplier used for pheromone evaporation")
    @Range(from = .1, to = .999)
    private double evaporation = 0.01;

    @Property(name = "Pheromone intensification rate", description = "Multiplier used for pheromone intensification")
    @Range(from = .1, to = .999)
    private double intensification = 0.02;

    @Property(name = "Random seed", description = "Random numbers generator seed")
    @Range(from = 1, to = 1000000)
    private int randomSeed = 1;

    public AntEvolutionStrategyConfig() {
        super();
        pheromoneInit = 10.0;
        pheromoneMin = 0.1;
        evaporation = 0.1;
        intensification = 0.2;
        randomSeed = 1;
    }

    public double getPheromoneInit() {
        return pheromoneInit;
    }

    public void setPheromoneInit(double pheromoneInit) {
        this.pheromoneInit = pheromoneInit;
    }

    public double getPheromoneMin() {
        return pheromoneMin;
    }

    public void setPheromoneMin(double pheromoneMin) {
        this.pheromoneMin = pheromoneMin;
    }

    public double getEvaporation() {
        return evaporation;
    }

    public void setEvaporation(double evaporation) {
        this.evaporation = evaporation;
    }

    public double getIntensification() {
        return intensification;
    }

    public void setIntensification(double intensification) {
        this.intensification = intensification;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }


}
