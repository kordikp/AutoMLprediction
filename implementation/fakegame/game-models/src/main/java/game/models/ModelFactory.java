package game.models;

import configuration.CfgTemplate;
import game.data.GameData;
import game.data.MinMaxDataNormalizer;
import configuration.models.ModelConfig;

public class ModelFactory {

    protected ModelFactory() {

    }

    public static ModelBox createModelBox(CfgTemplate what, GameData data, boolean learn) {
        ModelBox m = new ModelBox();
        int output = data.getONumber();
        for (int i = 0; i < output; i++) {
            ((ModelConfig) what).setTargetVariable(i);
            m.addModel(createNewConnectableModel(what, data, learn));
        }
        return m;
    }

    public static ConnectableModel createNewConnectableModel(CfgTemplate what, GameData data, boolean learn) {
        ConnectableModel model = getConnectableModel();
        ModelConfig cfg = (ModelConfig) what;
        int num = data.getInstanceNumber();
        //   cfg.setMaxLearningVectors(num);
        MinMaxDataNormalizer norm = new MinMaxDataNormalizer();
        norm.init(data.getInputVectors(), data.getOutputAttrs());
        model.init(cfg, data.getInputFeatures(), norm);
        ((ModelLearnable) model.getModel()).setMaxLearningVectors(num);

        if (learn) {
            ModelLearnable mo = (ModelLearnable) model.getModel();
            for (int i = 0; i < num; i++) {
                data.publishVector(i);
                model.storeLearningVector(data.getTargetOutput(cfg.getTargetVariable()));
            }
            mo.learn();
        }
        return model;
    }

    public static ModelLearnable createNewModel(CfgTemplate what, GameData data, boolean learn) {
        return (ModelLearnable) createNewConnectableModel(what, data, learn).getModel();
    }


    @Deprecated
    public static ConnectableModel createNewConnectableModel(CfgTemplate what, GameData data) {
        return createNewConnectableModel(what, data, true);
    }

    @Deprecated
    public static ModelLearnable createNewModel(CfgTemplate what, GameData data) {
        return (ModelLearnable) createNewConnectableModel(what, data).getModel();
    }

    private static ConnectableModel getConnectableModel() {
        return new ConnectableModel();
    }


}
