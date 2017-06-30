package configuration.models.ensemble;

import game.models.ensemble.ModelDivide;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import java.text.DecimalFormat;

/**
 * Configuration bean of the divide ensemble of models
 */
@Component(name = "DivideModelConfig", description = "Configuration of the Divide ensembling strategy")
public class DivideModelConfig extends ModelEnsembleConfigBase {
    @Property(name = "Cluster size multipliers", description = "Multiplies size of clusters, making them overlap, which makes model outputs to be composed more smoothly.")
    protected double clusterSizeMultiplier;

    public DivideModelConfig() {
        super();
        clusterSizeMultiplier = 1;
        classRef = ModelDivide.class;
    }

    protected String variablesToString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "(mult=" + df.format(clusterSizeMultiplier) + ")";
    }

    public double getClusterSizeMultiplier() {
        return clusterSizeMultiplier;
    }

    public void setClusterSizeMultiplier(double clusterSizeMultiplier) {
        if (clusterSizeMultiplier <= 1) this.clusterSizeMultiplier = 1;
        else this.clusterSizeMultiplier = clusterSizeMultiplier;
    }

}
