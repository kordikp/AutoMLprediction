package configuration.models;

import configuration.CfgTemplate;


/**
 * Configuration beans where trainer can be selected
 */
public interface TrainerSelectable extends ModelConfig {
    public Class getTrainerClass();

    public String getTrainerClassName();

    public void setTrainerClassName(String trainerClass);

    public CfgTemplate getTrainerCfg();

    public void setTrainerCfg(CfgTemplate trainerCfg);

}