package game.classifiers;

import static org.junit.Assert.assertTrue;

import configuration.CfgTemplate;
import game.data.AbstractGameData;
import game.data.FileGameData;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.single.PolynomialModelConfig;

import java.text.DecimalFormat;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class PolynomialClassifierTest {
    private static final String cfgfilename = "poly2Classify.properties";
    private static final String datafilename = "data/iris.txt";
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {

        data = new FileGameData(datafilename);


        PolynomialModelConfig pmc;
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);
        pmc = new PolynomialModelConfig();
        pmc.setTrainerClassName("QuasiNewtonTrainer");
        pmc.setTrainerCfg(new QuasiNewtonConfig());    // trained by LMS, in case of singular matrix QN is used
        pmc.setMaxDegree(2);
        clc.addClassModelCfg(pmc);
        clc.setDescription("Polynomial classifier with maximum degree 2");
        ConfigurationFactory.saveConfiguration(clc, cfgfilename);

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        c = ClassifierFactory.createNewClassifier(conf, data, true);
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