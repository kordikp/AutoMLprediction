package game.classifiers;

import configuration.CfgTemplate;
import game.classifiers.ensemble.ClassifierGAME;
import game.classifiers.single.ClassifierModel;
import game.data.FileGameData;
import game.data.GameData;
import game.evolution.DeterministicCrowdingStrategy;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.ensemble.GAMEClassifierConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.evolution.GeneticEvolutionStrategyConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.single.SigmoidModelConfig;

/**
 * Created by IntelliJ IDEA.
 * User: kordikp
 * Date: 21.3.2010
 * Time: 18:32:41
 * To change this template use File | Settings | File Templates.
 */

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class GAMEClassifierTest {
    static Logger logger = Logger.getLogger(GAMEClassifierTest.class);
    private static final String cfgfilename = "cfg/classifier_GAME-sigmoid.properties";
    private static final String datafilename = "data/iris.txt";
    private static final String desc = "GAME clasifier with sigmoidal models";
    static CfgTemplate generatedCfg;
    static GameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() throws IOException {
        //PropertyConfigurator.configure("log4j.properties");
        SigmoidModelConfig lmc = new SigmoidModelConfig();
        lmc.setTrainerClassName("QuasiNewtonTrainer");
        lmc.setTrainerCfg(new QuasiNewtonConfig());

         /*   SigmoidModelConfig snmc =  new SigmoidModelConfig();
            snmc.setTrainerClassName("QuasiNewtonTrainer");
            snmc.setTrainerCfg(new QuasiNewtonConfig());
            SineModelConfig smc =new SineModelConfig();
            smc.setTrainerClassName("QuasiNewtonTrainer");
            smc.setTrainerCfg(new QuasiNewtonConfig());
       */
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);
        clc.addClassModelCfg(lmc);
        clc.setClassRef(ClassifierModel.class);

        GAMEClassifierConfig gen = new GAMEClassifierConfig();
        gen.setClassifiersNumber(10);
        gen.setGenerations(10);
        gen.setLearnValidRatio(30);
        gen.setIncreasingComplexity(true);
        gen.setPropabilities(true);
        //gen.addBaseModelCfg(SineNormModel.class, smc);
        //  gen.addBaseModelCfg(SigmoidModel.class, snmc);
        gen.addBaseClassifierCfg(clc);
        //    gen.addBaseModelCfg(SineModel.class, smc);

        //gen.setEvolutionStrategy(new GeneticEvolutionStrategy());
        //GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        gen.setEvolutionStrategy(DeterministicCrowdingStrategy.class);
        GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new RandomEvolutionStrategy());
        //BaseEvolutionStrategyConfig cf = new BaseEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new AntEvolutionStrategy());
        //AntEvolutionStrategyConfig cf = new AntEvolutionStrategyConfig();


        //cf.setElitism(true);
        //cf.setElitists(1);
        cf.setSingleSolution(true);
        cf.setMaxSurvivals(3);

        gen.setEvolutionStrategyConfig(cf);

        generatedCfg = gen;
        generatedCfg.setDescription(desc);
        ConfigurationFactory.saveConfiguration(generatedCfg, cfgfilename);

        data = new FileGameData(datafilename);
    }

    @Test
    public void testConfigurationOfClassifiers() {

        CfgTemplate conf;
        try {
            generateConfigurationOfModelsAndLoadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        for (int i = 0; i < data.getONumber(); i++) {
            c = ClassifierFactory.createNewClassifier(conf, data, true);
        }
    }
}
