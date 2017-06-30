package game.classifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import configuration.CfgTemplate;
import game.classifiers.single.ClassifierModel;
import game.data.AbstractGameData;
import game.data.FileGameData;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;

import java.text.DecimalFormat;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class ClassifierModelFromFileTest {
    private static final String modelfilename = "cfg/model_genetic_evolvable_ensemble.properties";
    private static final String cfgfilename = "cfg/classifier_model_evolvable.properties";
    private static final String datafilename = "data/iris.txt";
    static CfgTemplate generatedCfg;
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {

        data = new FileGameData(datafilename);


        int outputs = data.getONumber(); //number of output attributes (classes)
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        CfgTemplate modelcfg = ConfigurationFactory.getConfiguration(modelfilename);
        ((ModelConfig) modelcfg).setMaxLearningVectors(500);
        ((ModelConfig) modelcfg).setMaxInputsNumber(10);
        clc.addClassModelCfg((CfgTemplate) modelcfg);

        generatedCfg = clc;
        generatedCfg.setDescription("Classifier with polynomial models deg 10");

        ConfigurationFactory.saveConfiguration(generatedCfg, cfgfilename);

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf = ConfigurationFactory.getConfiguration(cfgfilename);
        //TODO loadnout klasifikator.
        c = ClassifierFactory.createNewClassifier(conf, data, true);
        //Classifiers.getInstance().createNewClassifier(conf);
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
            //System.out.println("props[out|target]:" + outs + "class predicted:" + ((ConnectableClassifier) c).getOutput());
            for (int i = 0; i < data.getONumber(); i++) {
                err += Math.pow(out[i] - data.getTargetOutput(i), 2);

            }
        }
        System.out.println("Overall RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
    }
}