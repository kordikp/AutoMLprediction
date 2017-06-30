package configuration.classifiers;

import configuration.CommonUnits;
import configuration.classifiers.ensemble.*;
import game.classifiers.ensemble.*;

public class ClassifierEnsembleUnits extends CommonUnits {

    private static ClassifierEnsembleUnits instance;

    protected ClassifierEnsembleUnits() {
        setValue(ClassifierBagging.class, ClassifierBaggingConfig.class);
        setValue(ClassifierBoosting.class, ClassifierBoostingConfig.class);
        setValue(ClassifierCascadeGenProb.class, ClassifierCascadeGenProbConfig.class);
        setValue(ClassifierCascading.class, ClassifierCascadingConfig.class);
        setValue(ClassifierDelegating.class, ClassifierDelegatingConfig.class);
        setValue(ClassifierStackingProb.class, ClassifierStackingProbConfig.class);
        setValue(ClassifierArbitrating.class, ClassifierArbitratingConfig.class);
    }

    public static ClassifierEnsembleUnits getInstance() {
        if (instance == null) {
            instance = new ClassifierEnsembleUnits();
        }
        return instance;
    }
}
