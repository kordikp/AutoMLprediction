package configuration.models.ensemble;

import game.models.ensemble.ModelAreaSpecialization;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 21.12.2009
 * Time: 22:45:51
 * To change this template use File | Settings | File Templates.
 */
@Component(name = "ModelAreaSpecialization", description = "Configuration of the Area Specialization ensembling strategy")
public class AreaSpecializationModelConfig extends ModelEnsembleConfigBase {
    @Property(name = "Area size", description = "Number of nearest vectors which are taken into account when selecting best model")
    protected int area;

    @Property(name = "Next models specialization", description = "Determines how much will be next model specialized " +
            "on wrong responses by previous model")
    protected double modelsSpecialization;

    public AreaSpecializationModelConfig() {
        super();
        area = 7;
        modelsSpecialization = 5;
        classRef = ModelAreaSpecialization.class;
    }

    protected String variablesToString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "(area=" + area + ",spec=" + df.format(modelsSpecialization) + ")";
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public double getModelsSpecialization() {
        return modelsSpecialization;
    }

    public void setModelsSpecialization(double modelsSpecialization) {
        this.modelsSpecialization = modelsSpecialization;
    }

}
