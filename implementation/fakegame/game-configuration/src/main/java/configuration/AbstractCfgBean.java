package configuration;

/**
 * abstract class for trainer configs
 */
public abstract class AbstractCfgBean implements CfgTemplate {

    protected Class classRef;
    protected String description;

    public Class getClassRef() {
        return classRef;
    }

    public void setClassRef(Class classRef) {
        this.classRef = classRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AbstractCfgBean clone() {
        AbstractCfgBean newObject;
        try {
            newObject = (AbstractCfgBean) super.clone();
            return newObject;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String toString() {
        return this.getClass().getSimpleName().replace("Config", "") + variablesToString();
    }

    protected String variablesToString() {
        return "";
    }

}

