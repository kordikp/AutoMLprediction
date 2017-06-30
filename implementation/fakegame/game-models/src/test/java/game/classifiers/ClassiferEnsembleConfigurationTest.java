package game.classifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import configuration.CfgTemplate;
import game.classifiers.ensemble.ClassifierBagging;
import game.classifiers.single.ClassifierModel;
import game.data.FileGameData;
import game.data.GameData;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.ensemble.ClassifierBaggingConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.PSOConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.single.GaussianMultiModelConfig;
import configuration.models.single.LinearModelConfig;

import java.text.DecimalFormat;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class ClassiferEnsembleConfigurationTest {
    private static final String cfgfilename = "baggingClassifiers.properties";
    private static final String datafilename = "data/iris.txt";
    private static CfgTemplate generatedCfg;
    private static GameData data;
    static Classifier c;

    //  static WekaToGAMEInstancesFactory instancesFactory;
    // static WekaToGAMEClassifierFactory classifierFactory;
    @BeforeClass
    public static void generateConfigurationOfClassifiersAndLoadData() {

        //one classifier in the ensemble will be ClassifierModel (Model for enach output class)
        GaussianMultiModelConfig gmc = new GaussianMultiModelConfig();
        gmc.setTrainerClassName("QuasiNewtonTrainer");
        gmc.setTrainerCfg(new QuasiNewtonConfig());
        LinearModelConfig lmcpso = new LinearModelConfig();
        lmcpso.setTrainerClassName("PSOTrainer");
        lmcpso.setTrainerCfg(new PSOConfig());
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);
        clc.addClassModelCfg(lmcpso);
        clc.addClassModelCfg(gmc);
        clc.setClassRef(ClassifierModel.class);

        //todo second classifier in the ensemble will be Weka decision tree

        ClassifierBaggingConfig bagc = new ClassifierBaggingConfig();
        bagc.setClassifiersNumber(2);
        bagc.setBaseClassifiersDef(BaseModelsDefinition.UNIFORM);
        bagc.addBaseClassifierCfg(clc);

        generatedCfg = bagc;
        ConfigurationFactory.saveConfiguration(generatedCfg, cfgfilename);

        data = new FileGameData(datafilename);

    }

    @Test
    public void testConfigurationOfClassifiers() {

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
            //System.out.println("props[out|target]:" + outs + "class predicted:" + ((ConnectableClassifier) c).getOutput());
            for (int i = 0; i < data.getONumber(); i++) {
                err += Math.pow(out[i] - data.getTargetOutput(i), 2);

            }
        }
        System.out.println("Overall RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
    }
}