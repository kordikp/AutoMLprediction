package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierStacking;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierStackingConfig", description = "Configuration of the classifiers Stacking ensemble")
public class ClassifierStackingConfig extends EnsembleClassifierConfigBase {

    public ClassifierStackingConfig() {
        super();
        classRef = ClassifierStacking.class;
    }
}
