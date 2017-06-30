package configuration.models;

import configuration.CfgTemplate;

/**
 * Basic configuration beans for models.
 */
public interface ModelConfig extends CfgTemplate {
    public int getMaxLearningVectors();

    public void setMaxLearningVectors(int numberOfVectors);

    public String getName();

    public void setName(String name);

    public int getTargetVariable();

    public void setTargetVariable(int targetVariable);

    public int getMaxInputsNumber();

    public void setMaxInputsNumber(int maxInputsNumber);
}
