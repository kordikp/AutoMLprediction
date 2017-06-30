package game.evolution;

import java.util.ArrayList;

/**
 * The strategy design pattern for evolution of connectable models and or models in the ensemble
 */
public interface EvolutionStrategy {
    public void init(Object cfgBean, EvolutionContext context);

    public <T extends ObjectEvolvable> ArrayList<T> newGeneration(ArrayList<T> oldPopulation);

    /**
     * This function returns final population - removes all individuals except the elite (one or multiple for niching strategies)
     *
     * @param evolvedPopulation population in the last generation
     * @return surviving individuals
     */
    public <T extends ObjectEvolvable> ArrayList<T> getFinalPopulation(ArrayList<T> evolvedPopulation);

    public boolean isFinished();
}
