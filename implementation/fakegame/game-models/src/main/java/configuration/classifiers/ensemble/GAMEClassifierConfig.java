package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierGAME;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

/**
 * Configures the GAME ensemblig strategy
 */
@Component(name = "GAMEClassifierConfig", description = "Configuration of the GAME classifier ensembling strategy")
public class GAMEClassifierConfig extends EvolvableEnsembleClassifierConfig {

    @Property(name = "Limit the number of inputs to neuron by index of its layer")
    private boolean increasingComplexity;

    @Property(name = "Classifiers propabilities are used to extend inputs (false = class index is used)")
    private boolean propabilities;


    public GAMEClassifierConfig() {
        increasingComplexity = true;
        classRef = ClassifierGAME.class;
    }


    public boolean isPropabilities() {
        return propabilities;
    }

    public void setPropabilities(boolean propabilities) {
        this.propabilities = propabilities;
    }

    public boolean isIncreasingComplexity() {
        return increasingComplexity;
    }

    public void setIncreasingComplexity(boolean increasingComplexity) {
        this.increasingComplexity = increasingComplexity;
    }

}