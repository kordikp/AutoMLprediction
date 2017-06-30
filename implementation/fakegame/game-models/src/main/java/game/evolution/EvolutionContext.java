package game.evolution;

import java.util.ArrayList;

/**
 * This interface defines one side of the communication interface between evolution strategy and object holding evolved objects (context in strategy design pattern)
 */
public interface EvolutionContext {
    public <T extends ObjectEvolvable> void computeFitness(ArrayList<T> objects);

    public <T extends ObjectEvolvable> T produceOffspring(Dna dna);

    public <T extends ObjectEvolvable> T produceRandomOffspring();

    public <T extends ObjectEvolvable> double getDistance(T model1, T model2);
}
