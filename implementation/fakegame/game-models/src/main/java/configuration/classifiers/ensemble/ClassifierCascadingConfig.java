package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierCascading;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import java.text.DecimalFormat;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierCascadingConfig", description = "Configuration of the Cascading classifiers ensemble")
public class ClassifierCascadingConfig extends EnsembleClassifierConfigBase {
    @Property(name = "Threshold", description = "Threshold of certainty output needs to cross to be considered certain.")
    protected double threshold;


    public ClassifierCascadingConfig() {
        super();
        threshold = 0.7;
        classRef = ClassifierCascading.class;
    }

    protected String variablesToString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "(tr=" + df.format(threshold) + ")";
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        if (threshold > 1) this.threshold = 1;
        else this.threshold = threshold;
    }
}
