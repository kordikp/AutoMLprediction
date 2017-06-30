package game.models.evolution;

import configuration.CfgTemplate;
import configuration.models.ModelConfig;
import configuration.models.evolution.TreeEvolutionModelConfig;
import game.data.ArrayGameData;
import game.data.GameData;
import game.evolution.treeEvolution.evolutionControl.EvolutionControl;
import game.evolution.treeEvolution.run.ExperimentThread;
import game.models.Model;
import game.models.ModelLearnableBase;
import org.apache.log4j.Logger;

import java.util.concurrent.Semaphore;

/**
 * Created by frydatom on 23.9.16.
 */
public class TreeEvolutionModel extends ModelLearnableBase {
    private int computationTimeS;
    private Model model;
    private CfgTemplate cfg;

    public CfgTemplate getEvolvedConfig(){
        return cfg;
    }

    @Override
    public void init(ModelConfig cfg) {
        super.init(cfg);
        TreeEvolutionModelConfig cf = (TreeEvolutionModelConfig) cfg;
        computationTimeS = cf.getComputationTime();
    }

    @Override
    public Class getConfigClass() {
        return TreeEvolutionModelConfig.class;
    }

    @Override
    public void learn() {

        double output[][] = new double[target.length][1];
        for (int i = 0; i < target.length; i++) {
            output[i][0] = target[i];
        }

        GameData data = new ArrayGameData(inputVect, output);


        EvolutionControl evol = new EvolutionControl(data);
        evol.setRunTime(computationTimeS);

        evol.autoRun();
        model = (Model) evol.getBestModel();
        cfg = evol.getBestConfig();
    }

    @Override
    public double getOutput(double[] input_vector) {
        return model.getOutput(input_vector);
    }

    @Override
    public String toEquation(String[] inputEquation) {
        return model.toEquation(inputEquation);
    }
}
