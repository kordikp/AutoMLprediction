package configuration.models;

import configuration.CommonUnits;
import configuration.models.ensemble.*;
import game.models.ensemble.*;


public class ModelEnsembleUnits extends CommonUnits {

    private static ModelEnsembleUnits instance;

    protected ModelEnsembleUnits() {
        setValue(ModelBagging.class, BaggingModelConfig.class);
        setValue(ModelBoostingRT.class, BoostingRTModelConfig.class);
        setValue(ModelCascadeGen.class, CascadeGenModelConfig.class);
        setValue(ModelStacking.class, StackingModelConfig.class);
        setValue(ModelAreaSpecialization.class, AreaSpecializationModelConfig.class);
       // setValue(ModelDivide.class, DivideModelConfig.class);
    }

    public static ModelEnsembleUnits getInstance() {
        if (instance == null) {
            instance = new ModelEnsembleUnits();
        }
        return instance;
    }

}
