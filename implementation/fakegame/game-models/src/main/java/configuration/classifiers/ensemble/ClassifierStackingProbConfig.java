package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierStackingProb;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierStackingProbConfig", description = "Configuration of the classifiers Stacking ensemble using probabilities to get output.")
public class ClassifierStackingProbConfig extends EnsembleClassifierConfigBase {

    public ClassifierStackingProbConfig() {
        super();
        classRef = ClassifierStackingProb.class;
    }
}
