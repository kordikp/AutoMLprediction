package game.models;

import game.data.FileGameData;
import game.data.GameData;
import game.evolution.DeterministicCrowdingStrategy;
import game.models.ensemble.ModelGAME;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import configuration.evolution.GeneticEvolutionStrategyConfig;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.GAMEEnsembleModelConfig;

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class GAMEModelEnsemblerFileTest {
    static Logger logger = Logger.getLogger(GAMEModelEnsemblerFileTest.class);
    // private static final String modelfilename = "cfg/model_poly10.properties";
    private static final String cfgfilename = "cfg/model_GAME-poly1-5.properties";
    private static final String datafilename = "data/bosthouse.txt";
    private static GameData data;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() throws IOException {
        PropertyConfigurator.configure("log4j.properties");


        GAMEEnsembleModelConfig gen = new GAMEEnsembleModelConfig();
        gen.setModelsNumber(14);
        gen.setGenerations(30);
        gen.setLearnValidRatio(30);
        gen.setIncreasingComplexity(false);
        gen.setBaseModelsDef(BaseModelsDefinition.RANDOM);

        for (int i = 1; i < 6; i++) {
            String modelfilename = "cfg/model_poly" + i + ".properties";
            CfgTemplate modelcfg = ConfigurationFactory.getConfiguration(modelfilename);
            ((ModelConfig) modelcfg).setMaxLearningVectors(500);
            ((ModelConfig) modelcfg).setMaxInputsNumber(10);
            gen.addBaseModelCfg(modelcfg);
        }

        //gen.setEvolutionStrategy(GeneticEvolutionStrategy.class);
        //GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        gen.setEvolutionStrategy(DeterministicCrowdingStrategy.class);
        GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new RandomEvolutionStrategy());
        //BaseEvolutionStrategyConfig cf = new BaseEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new AntEvolutionStrategy());
        //AntEvolutionStrategyConfig cf = new AntEvolutionStrategyConfig();


        // cf.setElitism(true);
        //cf.setElitists(1);
        cf.setSingleSolution(true);
        cf.setMaxSurvivals(3);

        gen.setEvolutionStrategyConfig(cf);

        gen.setDescription("GAME model GA, single, 5 elite");
        ConfigurationFactory.saveConfiguration(gen, cfgfilename);
     

   /*     GAMEEnsembleModelConfig gen = new GAMEEnsembleModelConfig();
        gen.setModelsNumber(15);
        gen.setGenerations(30);
        gen.setLearnValidRatio(30);
        gen.setIncreasingComplexity(false);
        gen.setBaseModelsDef(BaseModelsDefinition.RANDOM);


        //load base model from file;
        for(int i=1;i<6;i++) {
        String modelfilename = "cfg/model_poly"+i+".properties";
        ClassWithConfigBean modelcfg = (ClassWithConfigBean)MainConfigurationTree.getInstance().loadObjectFromFile(modelfilename);
            ((ModelConfig)modelcfg.getCfgBean()).setMaxLearningVectors(500);
            ((ModelConfig)modelcfg.getCfgBean()).setMaxInputsNumber(10);
            gen.addBaseModelCfg(modelcfg.getClassRef(), (CfgTemplate)modelcfg.getCfgBean());
        }
      
        //gen.setEvolutionStrategy(new GeneticEvolutionStrategy());
        //GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        gen.setEvolutionStrategy(DeterministicCrowdingStrategy.class);
        GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new RandomEvolutionStrategy());
        //BaseEvolutionStrategyConfig cf = new BaseEvolutionStrategyConfig();

        //gen.setEvolutionStrategy(new AntEvolutionStrategy());
        //AntEvolutionStrategyConfig cf = new AntEvolutionStrategyConfig();


        cf.setElitism(true);
        cf.setElitists(1);
        cf.setSingleSolution(true);
        cf.setMaxSurvivals(3);

        gen.setEvolutionStrategyConfig(cf);

        generatedCfg = new ClassWithConfigBean(ModelGAME.class,gen);
        generatedCfg.setDescription("GAME model (poly1-5)");
        MainConfigurationTree.getInstance().saveObjectToFile(generatedCfg,cfgfilename);
   */
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