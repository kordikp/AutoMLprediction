package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierBoosting;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierBoostingConfig", description = "Configuration of the Boosting classifiers ensemble")
public class ClassifierBoostingConfig extends EnsembleClassifierConfigBase {

    public ClassifierBoostingConfig() {
        super();
        classRef = ClassifierBoosting.class;
    }

}
