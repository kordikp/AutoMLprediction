package configuration.classifiers.ensemble;

import game.classifiers.ensemble.ClassifierDelegating;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import java.text.DecimalFormat;

/**
 * Configuration bean of the bagging classifiers ensemble
 */
@Component(name = "ClassifierDelegatingConfig", description = "Configuration of the classifiers Delegating ensemble")
public class ClassifierDelegatingConfig extends EnsembleClassifierConfigBase {
    @Property(name = "Threshold", description = "Threshold of certainty output needs to cross to be considered certain.")
    protected double threshold;


    public ClassifierDelegatingConfig() {
        super();
        threshold = 0.7;
        classRef = ClassifierDelegating.class;
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
