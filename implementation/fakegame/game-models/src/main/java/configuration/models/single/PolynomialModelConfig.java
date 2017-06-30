package configuration.models.single;

import game.models.single.PolynomialModel;
import org.ytoh.configurations.annotations.Property;

/**
 * Configuration bean of the model with polynomial transfer function
 */
public class PolynomialModelConfig extends ModelSingleConfigBase {
    @Property(name = "Maximal degree", description = "Maximal degree of the polynomial")
    private int maxDegree;

    public PolynomialModelConfig() {
        super();
        setClassRef(PolynomialModel.class);
        maxDegree = 2;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    protected String variablesToString() {
        return "(degree=" + maxDegree + ")";
    }
}
