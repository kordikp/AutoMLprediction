package game.classifiers;

import static org.junit.Assert.assertTrue;

import configuration.CfgTemplate;
import game.data.FileGameData;
import game.data.GameData;
import game.evolution.DeterministicCrowdingStrategy;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.evolution.GeneticEvolutionStrategyConfig;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.GAMEEnsembleModelConfig;

import java.text.DecimalFormat;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class GAMEModellClassifierTest {
    private static final String cfgfilename = "cfg/classifier_modelGAME-increase.properties";
    private static final String datafilename = "C:\\Users\\kordikp\\Dropbox\\antro\\antro-class.txt";
    static GameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {

        data = new FileGameData(datafilename);

       /*
        LinearModelConfig lmc =  new LinearModelConfig();
                lmc.setTrainerClassName("QuasiNewtonTrainer");
                lmc.setTrainerCfg(new QuasiNewtonConfig());

                SigmoidModelConfig snmc =  new SigmoidModelConfig();
                snmc.setTrainerClassName("QuasiNewtonTrainer");
                snmc.setTrainerCfg(new QuasiNewtonConfig());
                SineModelConfig smc =new SineModelConfig();
                smc.setTrainerClassName("QuasiNewtonTrainer");
                smc.setTrainerCfg(new QuasiNewtonConfig());
              */


        GAMEEnsembleModelConfig gen = new GAMEEnsembleModelConfig();
        gen.setModelsNumber(10);
        gen.setGenerations(30);
        gen.setLearnValidRatio(30);
        gen.setIncreasingComplexity(false);
        gen.setMaxLayers(4);
        //gen.addBaseModelCfg(SineNormModel.class, smc);
        //   gen.addBaseModelCfg(SigmoidModel.class, snmc);
        //  gen.addBaseModelCfg(LinearModel.class, lmc);
        //  gen.addBaseModelCfg(SineModel.class, smc);
        gen.setBaseModelsDef(BaseModelsDefinition.RANDOM);

        for (int i = 1; i < 3; i++) {
            String modelfilename = "cfg/model_poly" + i + ".properties";
            CfgTemplate modelcfg = ConfigurationFactory.getConfiguration(modelfilename);
            ((ModelConfig) modelcfg).setMaxLearningVectors(200);
            ((ModelConfig) modelcfg).setMaxInputsNumber(5);
            gen.addBaseModelCfg(modelcfg);
        }
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
        cf.setMaxSurvivals(1);

        gen.setEvolutionStrategyConfig(cf);


        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        clc.addClassModelCfg(gen);
        clc.setDescription("GAME-DC model classifier(full complexity)");
        ConfigurationFactory.saveConfiguration(clc, cfgfilename);

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        ClassifierFactory.createNewClassifier(conf, data, true);
        assertTrue("Creation works", true);
    }


    @Test
    public void evaluateClassifier() {
        System.out.println(((ConnectableClassifier) c).toEquation());
        DecimalFormat formater = new DecimalFormat("#.#");
        double err = 0;
        for (int j = 0; j < data.getInstanceNumber(); j++) {
            data.publishVector(j);
            double[] out = ((ConnectableClassifier) c).getOutputProbabilities();
            String outs = "{";
            for (int i = 0; i < data.getONumber(); i++) {
                String o = formater.format(out[i]);
                String d = formater.format(data.getTargetOutput(i));
                outs += "[" + o + "|" + d + "]";
            }
            outs += ")";
            System.out.println("props[out|target]:" + outs + "class predicted:" + ((ConnectableClassifier) c).getOutput());
            for (int i = 0; i < data.getONumber(); i++) {
                err += Math.pow(out[i] - data.getTargetOutput(i), 2);

            }
        }
        System.out.println("Overall RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
    }
}