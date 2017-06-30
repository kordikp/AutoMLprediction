package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierCascadeGenProb;
import org.ytoh.configurations.annotations.Component;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierCascadeGenProbConfig", description = "Configuration of the Cascade Generalization classifiers ensemble using probabilities to compute output.")
public class ClassifierCascadeGenProbConfig extends EnsembleClassifierConfigBase {

    public ClassifierCascadeGenProbConfig() {
        super();
        classRef = ClassifierCascadeGenProb.class;
    }
}
