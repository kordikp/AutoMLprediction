package configuration.game.trainers;

import configuration.AbstractCfgBean;

public class PALDifferentialEvolutionConfig extends AbstractCfgBean {
    private int dim;

    /**
     * inicialises parametres to its default values
     */
    public PALDifferentialEvolutionConfig() {
        dim = 1;
    }


    /**
     * function to pass the values of parameters to the unit
     */
    public int getDim() {
        return dim;
    }
}
