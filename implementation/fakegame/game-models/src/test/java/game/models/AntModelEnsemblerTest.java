package game.models;

import configuration.CfgTemplate;
import game.data.FileGameData;
import game.data.GameData;
import game.evolution.AntEvolutionStrategy;
import game.models.ensemble.ModelEvolvableEnsemble;
import game.models.single.SigmoidModel;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.evolution.AntEvolutionStrategyConfig;
import configuration.game.trainers.DACOConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.EvolvableEnsembleModelConfig;
import configuration.models.single.LinearModelConfig;
import configuration.models.single.SigmoidModelConfig;
import configuration.models.single.SineModelConfig;

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class AntModelEnsemblerTest {
    private static final String cfgfilename = "antModelEnsemble.properties";
    private static final String datafilename = "../data/bosthouse.txt";
    static ModelLearnable linearModel;
    static GameData data;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {
        LinearModelConfig lmc = new LinearModelConfig();
        lmc.setTrainerClassName("QuasiNewtonTrainer");
        lmc.setTrainerCfg(new QuasiNewtonConfig());

        SigmoidModelConfig snmc = new SigmoidModelConfig();
        snmc.setTrainerClassName("QuasiNewtonTrainer");
        snmc.setTrainerCfg(new QuasiNewtonConfig());
        SineModelConfig smc = new SineModelConfig();
        smc.setTrainerClassName("DACOTrainer");
        smc.setTrainerCfg(new DACOConfig());

        EvolvableEnsembleModelConfig gen = new EvolvableEnsembleModelConfig();
        gen.setModelsNumber(5);
        gen.setGenerations(1);
        gen.setLearnValidRatio(30);
        gen.addBaseModelCfg(snmc);
        //gen.addBaseModelCfg(LinearModel.class, lmc);
        //gen.addBaseModelCfg(SineModel.class, smc);

        gen.setEvolutionStrategy(AntEvolutionStrategy.class);
        AntEvolutionStrategyConfig cf = new AntEvolutionStrategyConfig();
        cf.setRandomSeed(12345);
        gen.setEvolutionStrategyConfig(cf);
        gen.setDescription("Evolvable ensemble ANT, sine and sigmoid models");
        ConfigurationFactory.saveConfiguration(gen, cfgfilename);

        data = new FileGameData(datafilename);
    }

    @Test
    public void testModelFactory() {

        CfgTemplate conf;
        conf = (CfgTemplate) ConfigurationFactory.getConfiguration(cfgfilename);
        GameData gameData = new FileGameData(datafilename);
        ModelLearnable model = ModelFactory.createNewModel(conf, gameData, true);
    }
}