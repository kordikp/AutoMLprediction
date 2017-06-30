package configuration.models.ensemble;

import java.util.List;

import configuration.CfgTemplate;
import configuration.models.ModelConfig;

/**
 * Created by IntelliJ IDEA.
 * User: kordikp
 * Date: 11.10.2009
 * Time: 21:56:34
 * To change this template use File | Settings | File Templates.
 */
public interface EnsembleModelConfig extends ModelConfig {

    BaseModelsDefinition getBaseModelsDef();

    void setBaseModelsDef(BaseModelsDefinition baseModelsDef);

    List<CfgTemplate> getBaseModelCfgs();

    void setBaseModelCfgs(List<CfgTemplate> baseModelCfgs);

    void addBaseModelCfg(CfgTemplate baseModelCfg);

    int getModelsNumber();

    void setModelsNumber(int modelsNumber);
}
