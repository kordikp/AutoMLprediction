package configuration.evolution;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * base class to configure evolutionary strategies
 */
@Component(name = "BaseEvolutionStrategyConfig", description = "Configuration of the genetic optimization of models")
public class BaseEvolutionStrategyConfig {
    /*  @Property(name = "Elitism", description = "Use elitism in the evolutionary algorithm")
      protected boolean elitism;

      @Property(name = "Elitists number", description = "Number of elitists to be copied into next generation")
      @Slider(value=1,min=0,max=100,multiplicity=1,name="Elitists:")
      protected int elitists;
    */
    @Property(name = "Single solution", description = "Optimization strategy locates single optimum (false=multiple optima)")
    protected boolean singleSolution;
    @Property(name = "Survivals number", description = "Maximum number of optima (niche leaders)")
    protected int maxSurvivals;
    @Property(name = "Distance/error weight", description = "The weight of the distance when selecting surviving individuals")
    protected double distWeight = .2;

    public BaseEvolutionStrategyConfig() {
        super();
        //     elitism = true;
        //   elitists = 1;
        singleSolution = true;
        maxSurvivals = 3;
    }

  /*  public boolean isElitism() {
        return elitism;
    }

    public void setElitism(boolean elitism) {
        this.elitism = elitism;
    }

    public int getElitists() {
        return elitists;
    }

    public void setElitists(int elitists) {
        this.elitists = elitists;
    }
    */

    public double getDistWeight() {
        return distWeight;
    }

    public void setDistWeight(double distWeight) {
        this.distWeight = distWeight;
    }

    public boolean isSingleSolution() {
        return singleSolution;
    }

    public void setSingleSolution(boolean singleSolution) {
        this.singleSolution = singleSolution;
    }

    public int getMaxSurvivals() {
        return maxSurvivals;
    }

    public void setMaxSurvivals(int maxSurvivals) {
        this.maxSurvivals = maxSurvivals;
    }
}