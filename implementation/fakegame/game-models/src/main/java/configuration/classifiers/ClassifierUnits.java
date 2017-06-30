package configuration.classifiers;

import configuration.CommonUnits;
import configuration.classifiers.single.DTForestClassifierConfig;
import configuration.classifiers.single.DecisionTreeClassifierConfig;
import configuration.classifiers.single.NeuralClassifierConfig;
import configuration.classifiers.single.KNNClassifierConfig;
import game.classifiers.single.DTForestClassifier;
import game.classifiers.single.DecisionTreeClassifier;
import game.classifiers.single.KNNClassifier;
import game.classifiers.single.NeuralClassifier;


public class ClassifierUnits extends CommonUnits {

    private static ClassifierUnits instance;

    protected ClassifierUnits() {
        //setValue(WekaBayesNetClassifier.class, WekaBayesNetConfig.class);
        //setValue(WekaRandomForestClassifier.class, WekaRandomForestConfig.class);
      /*  setValue(RapidNaiveBayesClassifier.class, RapidNaiveBayesConfig.class);
        setValue(RapidDecisionTreeClassifier.class, RapidDecisionTreeConfig.class);
        setValue(RapidKNNClassifier.class, RapidKNNConfig.class);
        setValue(RapidSVMClassifier.class, RapidSVMConfig.class);
        setValue(RapidNeuralNetClassifier.class, RapidNeuralNetClassifierConfig.class);*/
     //   setValue(NeuralClassifier.class, NeuralClassifierConfig.class);
        setValue(DTForestClassifier.class, DTForestClassifierConfig.class);
        setValue(KNNClassifier.class, KNNClassifierConfig.class);
        setValue(DecisionTreeClassifier.class, DecisionTreeClassifierConfig.class);

    }

    public static ClassifierUnits getInstance() {
        if (instance == null) {
            instance = new ClassifierUnits();
        }
        return instance;
    }
}
