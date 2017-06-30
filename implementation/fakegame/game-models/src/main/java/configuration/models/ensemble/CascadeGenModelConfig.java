package configuration.models.ensemble;

import game.models.ensemble.ModelCascadeGen;


/**
 * Configuration bean of the Cascade Generalization Ensemble
 */
public class CascadeGenModelConfig extends ModelEnsembleConfigBase {
    public CascadeGenModelConfig() {
        super();
        classRef = ModelCascadeGen.class;
    }
}