package configuration.classifiers;

import configuration.CfgTemplate;

/**
 * Basic configuration beans for classifiers.
 */
public interface ClassifierConfig extends CfgTemplate {
    public int getMaxLearningVectors();

    public void setMaxLearningVectors(int numberOfVectors);

    public String getName();

    public void setName(String name);

    public int getMaxInputsNumber();

    public void setMaxInputsNumber(int maxInputsNumber);
}