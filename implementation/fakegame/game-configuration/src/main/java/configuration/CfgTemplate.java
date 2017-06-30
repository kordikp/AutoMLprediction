package configuration;

import java.io.Serializable;

/**
 * Configuration beans of neurons/trainers/models enables it to participate in modelling process
 */
public interface CfgTemplate extends Serializable {

    public Class getClassRef();

    public void setClassRef(Class classRef);

    public CfgTemplate clone();

    public String getDescription();

    public void setDescription(String description);
}
