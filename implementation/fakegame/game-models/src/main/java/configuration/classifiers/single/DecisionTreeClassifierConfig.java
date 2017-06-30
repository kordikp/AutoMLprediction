package configuration.classifiers.single;

import configuration.classifiers.ClassifierConfigBase;
import game.classifiers.single.DecisionTreeClassifier;


public class DecisionTreeClassifierConfig extends ClassifierConfigBase {

    public DecisionTreeClassifierConfig() {
        super();
        classRef = DecisionTreeClassifier.class;
    }

    protected String getComprehensiveClassName() {
        return "DecisionTree";
    }

}
