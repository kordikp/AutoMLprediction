package configuration.models.game;


import java.util.ArrayList;
import java.util.List;

import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.Table;

/**
 * This bean allows you to enable/disable individual neurons in the game algorithm overriding their allowedByDefault() flag
 */

@Component(name = "Neurons", description = "List of implemented neurons")
public class CfgNeurons {


    public List getCfgNeurons() {
        return cfgNeurons;
    }

    public void setCfgNeurons(List cfgNeurons) {
        this.cfgNeurons = cfgNeurons;
    }

    @Property(name = "Neurons")
    @Table
    private List cfgNeurons = new ArrayList();

    public CfgNeurons() {
        //     for (int i = 0; i < UnitLoader.getInstance().getNeuronsNumber(); i++) {
        //       Object cfgNeuron = UnitLoader.getInstance().getNeuronConfig(i);
        //  if(cfgNeuron != null) {
        //          cfgNeurons.add(cfgNeuron);
        //    }
        //  }

    }

    /**
     * ask if the GAME unit(neuron) is enabled for the GAME generation process
     *
     * @param which index of the neuron
     * @return true when allowed
     */

}
