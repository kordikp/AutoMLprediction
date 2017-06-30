package game.classifiers;

import configuration.CfgTemplate;
import game.data.GameData;
import game.data.MinMaxDataNormalizer;

public class ClassifierFactory {

    public static ConnectableClassifier createNewClassifier(Classifier c, GameData data) {
        ConnectableClassifier classifier = getClassifierConnectable();
        // MinMaxDataNormalizer norm = new MinMaxDataNormalizer();
        // norm.init(data.getInputVectors(),data.getOutputAttrs());
        classifier.init(c, data.getInputFeatures());
        //TODO co normalizace?.
        return classifier;
    }

    public static ConnectableClassifier createNewClassifier(CfgTemplate what, GameData data, boolean learn) {
        ConnectableClassifier classifier = getClassifierConnectable();
        int num = data.getInstanceNumber();
        MinMaxDataNormalizer norm = new MinMaxDataNormalizer();
        norm.init(data.getInputVectors(), data.getOutputAttrs());
        classifier.init(what, data.getInputFeatures(), norm);
        classifier.setMaxLearningVectors(num);


        if (learn) {
            for (int i = 0; i < num; i++) {
                data.publishVector(i);
                classifier.storeLearningVector(data.getTargetOutputs());
            }

            classifier.learn();
        }


        return classifier;
    }


    private static ConnectableClassifier getClassifierConnectable() {
        return new ConnectableClassifier();
    }


}
