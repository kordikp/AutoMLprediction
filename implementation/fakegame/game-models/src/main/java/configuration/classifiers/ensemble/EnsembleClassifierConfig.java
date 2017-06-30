package configuration.classifiers.ensemble;

import configuration.CfgTemplate;
import configuration.classifiers.ClassifierConfig;
import configuration.models.ensemble.BaseModelsDefinition;

import java.util.List;

/**
 * Interface for ensemble classifier configuration classes
 */
public interface EnsembleClassifierConfig extends ClassifierConfig {
    BaseModelsDefinition getBaseClassifiersDef();

    void setBaseClassifiersDef(BaseModelsDefinition baseModelsDef);

    List<CfgTemplate> getBaseClassifiersCfgs();

    void setBaseClassifiersCfgs(List<CfgTemplate> baseClassifiersCfgs);

    void addBaseClassifierCfg(CfgTemplate baseClassifierCfg);

    int getClassifiersNumber();

    void setClassifiersNumber(int modelsNumber);
}
