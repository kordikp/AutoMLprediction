package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierArbitrating;
import org.ytoh.configurations.annotations.Component;

@Component(name = "ClassifierArbitratingConfig", description = "Configuration of the Arbitrating classifiers ensemble")
public class ClassifierArbitratingConfig extends EnsembleClassifierConfigBase {

    public ClassifierArbitratingConfig() {
        super();
        classRef = ClassifierArbitrating.class;
    }

    //allow only even numbers (each classifier must have its own arbiter)
    public void setClassifiersNumber(int classifiersNumber) {
        if (classifiersNumber % 2 == 1) classifiersNumber++;
        this.classifiersNumber = classifiersNumber;
    }
}
