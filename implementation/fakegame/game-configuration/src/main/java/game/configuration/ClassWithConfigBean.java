package game.configuration;

import configuration.AbstractCfgBean;

import java.io.Serializable;

/**
 * Class to store information on model classes name and their configuration beans
 */
public class ClassWithConfigBean implements Serializable, Cloneable {

    private String description;
    private Class classRef;
    private Object cfgBean;


    public ClassWithConfigBean(Class modelClassName, Object cfgBean) {
        this.classRef = modelClassName;
        this.cfgBean = cfgBean;
    }

    public ClassWithConfigBean(String description, Class modelClassName, Object cfgBean) {
        this.classRef = modelClassName;
        this.cfgBean = cfgBean;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class getClassRef() {
        return classRef;
    }

    public void setClassRef(Class classRef) {
        this.classRef = classRef;
    }

    public Object getCfgBean() {
        return cfgBean;
    }

    public void setCfgBean(Object cfgBean) {
        this.cfgBean = cfgBean;
    }

    public ClassWithConfigBean clone() {
        ClassWithConfigBean newObject;
        try {
            newObject = (ClassWithConfigBean) super.clone();
            if (cfgBean != null) {
                if (cfgBean instanceof AbstractCfgBean) newObject.cfgBean = ((AbstractCfgBean) cfgBean).clone();
            }
            return newObject;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}
