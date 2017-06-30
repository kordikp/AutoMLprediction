package configuration;

import game.configuration.Configurable;

import java.util.ArrayList;
import java.util.List;

public class CommonUnits {

    protected CommonUnits() {

    }

    protected List<CfgTemplate> config = new ArrayList<CfgTemplate>();

    protected void setValue(Class<? extends Configurable> aClass, Class<? extends CfgTemplate> aConfig) {
        if (aConfig != null) {
            CfgTemplate cfg = null;
            try {
                cfg = aConfig.newInstance();
                cfg.setClassRef(aClass);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            config.add(cfg);
        }
    }

    public int getCount() {
        return config.size();
    }


    public CfgTemplate getUnitConfig(int i) {
        return config.get(i);
    }


}
