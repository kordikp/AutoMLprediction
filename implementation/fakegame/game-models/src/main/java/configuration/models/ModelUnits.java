package configuration.models;

import configuration.models.single.*;
import game.models.single.*;
import configuration.CommonUnits;

public class ModelUnits extends CommonUnits {

    private static ModelUnits instance;

    protected ModelUnits() {
        setValue(LinearModel.class, LinearModelConfig.class);
        setValue(PolynomialModel.class, PolynomialModelConfig.class);
        setValue(SigmoidModel.class, SigmoidModelConfig.class);
        setValue(GaussianModel.class, GaussianModelConfig.class);
        setValue(SineModel.class, SineModelConfig.class);
        setValue(ExpModel.class, ExpModelConfig.class);
        //setValue(RapidLocalPolyModel.class, RapidLocalPolyConfig.class);
    }

    public static ModelUnits getInstance() {
        if (instance == null) {
            instance = new ModelUnits();
        }
        return instance;
    }


}
