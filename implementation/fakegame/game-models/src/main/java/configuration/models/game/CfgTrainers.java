package configuration.models.game;


import java.util.ArrayList;
import java.util.List;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Table;

/**
 * This bean allows you to enable/disable individual optimization methods overriding their allowedByDefault() flag
 */
@Component(name = "Trainers", description = "List of optimization algorithms implemented")
public class CfgTrainers {

    public List getCfgTrainers() {
        return cfgTrainers;
    }

    public void setCfgTrainers(List cfgTrainers) {
        this.cfgTrainers = cfgTrainers;
    }

    @Property(name = "Traning methods")
    @Table
    private List cfgTrainers = new ArrayList();

    public CfgTrainers() {


    }

}
