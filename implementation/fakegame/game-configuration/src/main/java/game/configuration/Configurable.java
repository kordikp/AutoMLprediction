package game.configuration;

import java.io.Serializable;

/**
 * This interface is implemented by classes, that can be configured by config beans
 */
public interface Configurable extends Serializable {
    //  public void setConfig(Object configBean);
    //  public Object getConfig();
    public Class getConfigClass();
}
