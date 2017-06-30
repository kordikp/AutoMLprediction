package configuration.evolution;

import configuration.AbstractCfgBean;
import game.evolution.treeEvolution.evolutionControl.EvolutionControl;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.CheckBox;

/**
 * Configuration for automatic model evolution main process.
 */
public class EvolutionControlConfig extends AbstractCfgBean {

    @Property(name = "Time to run", description = "Approximate time in seconds that will be available for evolution run.")
    protected int runTime;

    @Property(name = "Use test set", description = "Indicates whether data will be divided into learn/valid or learn/valid/test sets.")
    @CheckBox
    protected boolean useTestSet;

    public EvolutionControlConfig() {
        classRef = EvolutionControl.class;
        runTime = 600;
        useTestSet = true;
    }

    public EvolutionControlConfig clone() {
        EvolutionControlConfig newObject;
        newObject = (EvolutionControlConfig) super.clone();
        return newObject;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public boolean getUseTestSet() {
        return useTestSet;
    }

    public void setUseTestSet(boolean useTestSet) {
        this.useTestSet = useTestSet;
    }
}
