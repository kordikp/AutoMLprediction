package game.models;

import configuration.CfgTemplate;
import game.data.FileGameData;
import game.data.GameData;
import game.evolution.GeneticEvolutionStrategy;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.evolution.GeneticEvolutionStrategyConfig;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.EvolvableEnsembleModelConfig;

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class GeneticModelEnsemblerTest {
    private static final String cfgfilename = "model_genetic_evolvable_ensemble.cfg";
    private static final String datafilename = "../data/bosthouse.txt";

    static CfgTemplate generatedCfg;
    private static GameData data;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {
        //      PropertyConfigurator.configure("log4j.properties");
       /* LinearModelConfig lmc =  new LinearModelConfig();
        lmc.setTrainerClassName("QuasiNewtonTrainer");
        lmc.setTrainerCfg(new QuasiNewtonConfig());

        SigmoidModelConfig snmc =  new SigmoidModelConfig();
        snmc.setTrainerClassName("QuasiNewtonTrainer");
        snmc.setTrainerCfg(new QuasiNewtonConfig());
        SineModelConfig smc =new SineModelConfig();
        smc.setTrainerClassName("DACOTrainer");
        smc.setTrainerCfg(new DACOConfig());
       */
        EvolvableEnsembleModelConfig gen = new EvolvableEnsembleModelConfig();
        gen.setModelsNumber(10);
        gen.setGenerations(30);
        gen.setLearnValidRatio(30);
        //gen.setMaxInputsNumber(2);

        //gen.addBaseModelCfg(SineNormModel.class, smc);
        //  gen.addBaseModelCfg(SigmoidModel.class, snmc);
        //  gen.addBaseModelCfg(LinearModel.class, lmc);
        // gen.addBaseModelCfg(SineModel.class, smc);

        gen.setBaseModelsDef(BaseModelsDefinition.RANDOM);

        for (int i = 1; i < 4; i++) {
            String modelfilename = "cfg/model_poly" + i + ".properties";
            CfgTemplate modelcfg = ConfigurationFactory.getConfiguration(modelfilename);
            ((ModelConfig) modelcfg).setMaxInputsNumber(5);
            gen.addBaseModelCfg(modelcfg);
        }
        gen.setEvolutionStrategy(GeneticEvolutionStrategy.class);
        GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();
        cf.setMutationRate(0.5);
        cf.setSingleSolution(true);
        cf.setMaxSurvivals(1);
        //  cf.setElitism(false);
        gen.setEvolutionStrategyConfig(cf);

        generatedCfg = gen;
        generatedCfg.setDescription("Evolvable model (sigmoid, genetic)");

        //TODO predelat na save konfigurace.
        ConfigurationFactory.saveConfiguration(generatedCfg, cfgfilename);

        data = new FileGameData(datafilename);
    }

    @Test
    public void testConfigurationOfModels() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        for (int i = 0; i < data.getONumber(); i++) {
            Models.getInstance().createNewModel(conf);
        }
    }
}