package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierBagging;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierBaggingConfig", description = "Configuration of the Bagging classifiers ensemble")
public class ClassifierBaggingConfig extends EnsembleClassifierConfigBase {

    public ClassifierBaggingConfig() {
        super();
        classRef = ClassifierBagging.class;
    }

}
