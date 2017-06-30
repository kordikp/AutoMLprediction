package game.classifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import configuration.CfgTemplate;
import game.classifiers.ensemble.ClassifierEvolvableEnsemble;
import game.classifiers.single.ClassifierModel;
import game.configuration.ClassWithConfigBean;
import game.data.AbstractGameData;
import game.data.FileGameData;
import game.evolution.GeneticEvolutionStrategy;
import game.models.single.PolynomialModel;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.ensemble.EvolvableEnsembleClassifierConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.evolution.GeneticEvolutionStrategyConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.EvolvableEnsembleModelConfig;
import configuration.models.single.PolynomialModelConfig;

import java.text.DecimalFormat;

/**
 * Do not forget to set working directory to core/trunk/target before running this test
 */
public class GeneticClassifierEnsemblerTest {
    private static final String cfgfilename = "cfg/classifier_evolvable-poly2.properties";
    private static final String datafilename = "gui-data/iris.txt";
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {
        PropertyConfigurator.configure("log4j.properties");

        data = new FileGameData(datafilename);

    /*  int outputs = data.getONumber(); //number of output attributes (classes)

        SigmoidModelConfig[] snmc =  new SigmoidModelConfig[outputs];
        SineModelConfig[] smc =new SineModelConfig[outputs];
        LinearModelConfig[] llmc = new LinearModelConfig[outputs];

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);
        for(int i=0;i<outputs;i++) {
            llmc[i] = new LinearModelConfig();
            llmc[i].setTrainerClassName("QuasiNewtonTrainer");
            llmc[i].setTrainerCfg(new QuasiNewtonConfig());
            llmc[i].setTargetVariable(i);

            snmc[i] = new SigmoidModelConfig();
            snmc[i].setTrainerClassName("QuasiNewtonTrainer");
            snmc[i].setTrainerCfg(new QuasiNewtonConfig());
            snmc[i].setTargetVariable(i);

            smc[i] = new SineModelConfig();
            smc[i].setTrainerClassName("QuasiNewtonTrainer");
            smc[i].setTrainerCfg(new QuasiNewtonConfig());
            smc[i].setTargetVariable(i);

            clc.addClassModelCfg(LinearModel.class, llmc[i]);
            clc.addClassModelCfg(SigmoidModel.class, snmc[i]);
            clc.addClassModelCfg(SineModel.class, smc[i]);
        }
      */
        PolynomialModelConfig pmc;
        pmc = new PolynomialModelConfig();
        pmc.setTrainerClassName("QuasiNewtonTrainer");
        pmc.setTrainerCfg(new QuasiNewtonConfig());    // trained by LMS, in case of singular matrix QN is used
        pmc.setMaxDegree(2);


        EvolvableEnsembleModelConfig gen1 = new EvolvableEnsembleModelConfig();
        gen1.setModelsNumber(10);
        gen1.setGenerations(10);
        gen1.setLearnValidRatio(30);
        //gen.setMaxInputsNumber(2);
        //gen.addBaseModelCfg(SineNormModel.class, smc);
        gen1.addBaseModelCfg(pmc);
        gen1.setEvolutionStrategy(GeneticEvolutionStrategy.class);
        GeneticEvolutionStrategyConfig cf = new GeneticEvolutionStrategyConfig();
        cf.setMutationRate(0.1);
        gen1.setEvolutionStrategyConfig(cf);

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);
        clc.addClassModelCfg(gen1);
        clc.setClassRef(ClassifierModel.class);

        EvolvableEnsembleClassifierConfig gen = new EvolvableEnsembleClassifierConfig();
        gen.setGenerations(10);
        gen.setLearnValidRatio(30);
        gen.setClassifiersNumber(10);
        //gen.setMaxInputsNumber(2);
        //gen.addBaseModelCfg(SineNormModel.class, smc);
        gen.addBaseClassifierCfg(clc);
        //  gen.addBaseModelCfg(LinearModel.class, lmc);
        // gen.addBaseModelCfg(SineModel.class, smc);

        gen.setEvolutionStrategy(GeneticEvolutionStrategy.class);
        GeneticEvolutionStrategyConfig cf2 = new GeneticEvolutionStrategyConfig();
        cf2.setMutationRate(0.5);
        //cf2.setElitism(true);
        //cf2.setElitists(1);
        cf2.setSingleSolution(true);
        //cf.setMaxSurvivals(3);

        gen.setEvolutionStrategyConfig(cf2);
        gen.setDescription("Evolvable classifier (polynomial models degree 2)");
        ConfigurationFactory.saveConfiguration(gen, cfgfilename);


    }

    @Test
    public void testConfigurationOfClassifers() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        c = ClassifierFactory.createNewClassifier(conf, data, true);
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