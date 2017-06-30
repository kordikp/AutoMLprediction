/**
 * @author Pavel Kordik
 * @version 0.90
 */
package configuration.game.trainers;

import configuration.AbstractCfgBean;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Range;
import org.ytoh.configurations.ui.CheckBox;

/**
 * Class for the SADE trainer configuration
 */
@Component(name = "SADETrainerConfig", description = "SADE algorithm configuration")
public class SADEConfig extends AbstractCfgBean {
    @Property(name = "Fitness call limit", description = "Limit of fitness function call (evolution stops, when reached)!")
    @Range(from = 0, to = 100000)
    int fitnessCallsLimit;
    @Property(name = "Interval, where initial solutions are generated from (10 = (-10,10)")
    @Range(from = 0, to = 1000)
    int domainSize;


    public boolean isReturnToDomain() {
        return returnToDomain;
    }

    public void setReturnToDomain(boolean returnToDomain) {
        this.returnToDomain = returnToDomain;
    }

    @Property(name = "ReturnToDomain", description = "Solutions outside the specified domain are not accepted")
    @CheckBox
    boolean returnToDomain;
    @Property(name = "Record error", description = "Error recorded every ... fitness function call")
    @Range(from = 0, to = 100)
    private int rec;

    public void setRec(int rec) {
        this.rec = rec;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    @Property(name = "Redraw screen", description = "Screen redrawed every ... fitness function call")
    @Range(from = 0, to = 5000)
    private int draw;

    public int getFitnessCallsLimit() {
        return fitnessCallsLimit;
    }

    public void setFitnessCallsLimit(int fitnessCallsLimit) {
        this.fitnessCallsLimit = fitnessCallsLimit;
    }

    public int getDomainSize() {
        return domainSize;
    }

    public void setDomainSize(int domainSize) {
        this.domainSize = domainSize;
    }

    /**
     * inicialises parametres to its default values
     */
    public SADEConfig() {
        rec = 10; //record
        draw = 100; //redraw
        fitnessCallsLimit = 3000;
        domainSize = 10;
        returnToDomain = false;
    }


    /**
     * function to pass the values of parameters to the unit
     *
     * @return frequency to redraw screen during optimization
     */
    public int getDraw() {
        return draw;
    }

    /**
     * function to pass the values of parameters to the unit
     *
     * @return frequency to record the error during optimization
     */
    public int getRec() {
        return rec;
    }


}
