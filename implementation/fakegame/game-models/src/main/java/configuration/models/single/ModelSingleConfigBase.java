package configuration.models.single;

import configuration.CfgTemplate;
import configuration.models.ModelConfigBase;
import configuration.models.TrainerSelectable;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.DynamicDropDown;
import org.ytoh.configurations.ui.CheckBox;

/**
 * Abstract class for config methods
 * Author: cernyjn
 */
public abstract class ModelSingleConfigBase extends ModelConfigBase implements TrainerSelectable {
    /**
     * Class name of the trainer used to train model.
     */
    @Property(name = "Default optimization method: (none=random)")
    @DynamicDropDown(type = String.class, key = "trainers")
    protected String trainerClassName = "QuasiNewtonTrainer";
    @Property(name = "Configuration bean of selected trainer")
    protected CfgTemplate trainerCfg;

    public Class getTrainerClass() {
        Class cls = null;
        try {
            cls = getClass().getClassLoader().loadClass("game.trainers." + trainerClassName);
//            cls = ClassLoader.getSystemClassLoader().loadClass("game.trainers."+ trainerClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return cls;
    }

    public String getTrainerClassName() {
        return trainerClassName;
    }

    public void setTrainerClassName(String trainerClassName) {
        this.trainerClassName = trainerClassName;
    }

    /**
     * Configuration bean of the trainer used to train model.
     */
    public CfgTemplate getTrainerCfg() {
        return trainerCfg;
    }

    public void setTrainerCfg(CfgTemplate trainerCfg) {
        this.trainerCfg = trainerCfg;
    }

    @Property(name = "Validation set size", description = "Set 0% to disable validation")
    //@Slider(value=30,min=0,max=99,multiplicity=1,name="Validate on [%] of training data:")
    private int validationPercent = 30;

    public int getValidationPercent() {
        return validationPercent;
    }

    public void setValidationPercent(int validationPercent) {
        this.validationPercent = validationPercent;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    @Property(name = "Enable validation", description = "part of the training set will be used to prevent overtraining")
    private boolean validationEnabled = true;            // print debug info

}
