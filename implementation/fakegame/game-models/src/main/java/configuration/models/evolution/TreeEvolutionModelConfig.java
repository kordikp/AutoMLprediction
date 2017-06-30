package configuration.models.evolution;

import configuration.models.ModelConfigBase;
import game.models.evolution.TreeEvolutionModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Created by frydatom on 23.9.16.
 */
public class TreeEvolutionModelConfig extends ModelConfigBase {

   // @Property(name="number of threads", description = "number of threads used for evolution")
   // private int numberOfThreads;

    @Property(name="computation time", description = "computation time in s")
    private int computationTime;

    //@Property(name="repeats", description="number of times is the experiment repeated")
    //private int repeats;
//
//    public int getNumberOfThreads() {
//        return numberOfThreads;
//    }
//
//    public void setNumberOfThreads(int numberOfThreads) {
//        this.numberOfThreads = numberOfThreads;
//    }

    /**
     * Returns preferred computation time in seconds
     * @return computationTime in seconds
     */
    public int getComputationTime() {
        return computationTime;
    }

    /**
     * Sets preferred computation time in seconds
     * @param computationTime
     */
    public void setComputationTime(int computationTime) {
        this.computationTime = computationTime;
    }

//    public int getRepeats() {
//        return repeats;
//    }
//
//    public void setRepeats(int repeats) {
//        this.repeats = repeats;
//    }

    public TreeEvolutionModelConfig(){
        super();
        setClassRef(TreeEvolutionModel.class);
    }
}
