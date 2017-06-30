package configuration.models;

import java.util.ArrayList;
import java.util.List;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Range;
import org.ytoh.configurations.annotations.Table;

import configuration.models.game.CfgGame;

/**
 * This bean configures models in general
 */
@Component(name = "Data mining models", description = "select models to be generated")
public class CfgModel {
    // tady bych potreboval udelat list trid, ktere implementuji interface Model
    // tj. napriklad list checkboxu

    public List getModelCfgBeans() {
        return modelCfgBeans;
    }

    public void setModelCfgBeans(List modelCfgBeans) {
        this.modelCfgBeans = modelCfgBeans;
    }

    @Property(name = "Models")
    @Table
    private List modelCfgBeans = new ArrayList();

    @Property(name = "Linear models")
    private boolean linear = false;

    @Property(name = "Slow models")
    private boolean slow = false;

    @Property(name = "Number of models")
    @Range(from = 1, to = 100)
    private int c = 5;

    @Property(name = "Configure GAME algorithm")
    private CfgGame bean = new CfgGame();

    public CfgGame getBean() {
        return bean;
    }

    public void setBean(CfgGame bean) {
        this.bean = bean;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public boolean isSlow() {
        return slow;
    }

    public void setSlow(boolean slow) {
        this.slow = slow;
    }

    public boolean isLinear() {
        return linear;
    }

    public void setLinear(boolean lin) {
        this.linear = lin;
    }

    public CfgModel() {
        //  for (int i = 0; i < UnitLoader.getInstance().getModelsNumber(); i++) {
        //     Object cfg = UnitLoader.getInstance().getModelConfig(i);
        //     if(cfg != null) modelCfgBeans.add(cfg);
        // }
    }
}
