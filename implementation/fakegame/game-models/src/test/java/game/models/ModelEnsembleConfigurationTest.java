package game.models;

import configuration.CfgTemplate;
import game.data.FileGameData;
import game.data.GameData;
import game.models.ensemble.ModelBagging;
import game.models.ensemble.ModelBoostingRT;
import game.models.single.LinearModel;
import game.models.single.PolynomialModel;
import game.models.single.SineModel;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.game.trainers.DACOConfig;
import configuration.game.trainers.PSOConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaggingModelConfig;
import configuration.models.ensemble.BoostingRTModelConfig;
import configuration.models.single.LinearModelConfig;
import configuration.models.single.PolynomialModelConfig;
import configuration.models.single.SineModelConfig;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class ModelEnsembleConfigurationTest {
    private static final String cfgfilename = "ensemble.cfg";
    private static final String datafilename = "../data/iris.txt";
    private static GameData data;
    static ModelLearnable linearModel;


    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {
        LinearModelConfig lmc = new LinearModelConfig();
        lmc.setTrainerClassName("QuasiNewtonTrainer");
        lmc.setTrainerCfg(new QuasiNewtonConfig());

        LinearModelConfig lmcpso = new LinearModelConfig();
        lmcpso.setTrainerClassName("PSOTrainer");
        lmcpso.setTrainerCfg(new PSOConfig());
        SineModelConfig smc = new SineModelConfig();
        smc.setTrainerClassName("DACOTrainer");
        smc.setTrainerCfg(new DACOConfig());
        BoostingRTModelConfig boostc = new BoostingRTModelConfig();
        boostc.setModelsNumber(3);

        boostc.addBaseModelCfg(smc);
        boostc.addBaseModelCfg(lmc);
        boostc.addBaseModelCfg(lmc);

        BaggingModelConfig bc = new BaggingModelConfig();
        bc.setModelsNumber(4);
        bc.addBaseModelCfg(lmc);
        bc.addBaseModelCfg(lmcpso);
        bc.addBaseModelCfg(boostc);
        bc.addBaseModelCfg(new PolynomialModelConfig());
        ConfigurationFactory.saveConfiguration(bc, cfgfilename);

        data = new FileGameData(datafilename);
    }

    @Test
    public void testConfigurationOfModels() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        // for (int i = 0; i < data.getONumber(); i++) {
        ModelFactory.createNewConnectableModel(conf, data, true);
        //}
    }
}
