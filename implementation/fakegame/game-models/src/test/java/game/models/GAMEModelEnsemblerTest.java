package game.models;

import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.single.PolynomialModelConfig;
import game.data.FileGameData;
import game.data.GameData;
import game.evolution.AntEvolutionStrategy;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import configuration.evolution.AntEvolutionStrategyConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.GAMEEnsembleModelConfig;

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class GAMEModelEnsemblerTest {
    static Logger logger = Logger.getLogger(GAMEModelEnsemblerTest.class);
    private static final String cfgfilename = "model_GAME-ANT.properties";
    private static final String datafilename = "data/bosthouse.txt";
    static ConnectableModel model;
    static GameData data;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() throws IOException {
        PropertyConfigurator.configure("log4j.properties");

        GAMEEnsembleModelConfig gen = new GAMEEnsembleModelConfig();
        gen.setModelsNumber(10);
        gen.setGenerations(10);
        gen.setLearnValidRatio(30);
        gen.setIncreasingComplexity(false);
        gen.setBaseModelsDef(BaseModelsDefinition.RANDOM);
        gen.setMaxLayers(5);
        PolynomialModelConfig[] smci;
        smci = new PolynomialModelConfig[4];
        for (int i = 1; i < 3; i++) {
            smci[i] = new PolynomialModelConfig();
            smci[i].setTrainerClassName("QuasiNewtonTrainer");
            smci[i].setTrainerCfg(new QuasiNewtonConfig());
            smci[i].setMaxDegree(i);
            smci[i].setMaxLearningVectors(200);
            smci[i].setMaxInputsNumber(2);
            smci[i].setDescription("Polynomial model with max degree " + i);
            gen.addBaseModelCfg(smci[i]);
        }
        // gen.setEvolutionStrategy(GeneticEvolutionStrategy.class);
        // GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        //  gen.setEvolutionStrategy(DeterministicCrowdingStrategy.class);
        //  GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        //  gen.setEvolutionStrategy(RandomEvolutionStrategy.class);
        //  BaseEvolutionStrategyConfig cf = new BaseEvolutionStrategyConfig();

        gen.setEvolutionStrategy(AntEvolutionStrategy.class);
        AntEvolutionStrategyConfig cf = new AntEvolutionStrategyConfig();


        //cf.setElitism(false);
        //cf.setElitists(1);
        cf.setSingleSolution(true);
        cf.setMaxSurvivals(1);
        //cf.setMutationRate(0.1);
        gen.setEvolutionStrategyConfig(cf);

        gen.setDescription("GAME polynomial(1,2) model ANT optimization");
        ConfigurationFactory.saveConfiguration(gen, cfgfilename);


        data = new FileGameData(datafilename);

    }

    @Test
    public void testConfigurationOfModels() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        for (int i = 0; i < data.getONumber(); i++) {
            logger.info("Building model " + i);
            model = ModelFactory.createNewConnectableModel(conf, data, true);
            System.out.println(model.toEquation());
            double err = 0;
            for (int j = 0; j < data.getInstanceNumber(); j++) {
                data.publishVector(j);
                err += Math.pow(model.getOutput() - data.getTargetOutput(i), 2);

            }
            System.out.println("RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
        }
    }
}