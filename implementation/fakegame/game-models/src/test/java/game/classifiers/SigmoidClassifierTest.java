package game.classifiers;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.single.PolynomialModelConfig;
import configuration.models.single.SigmoidModelConfig;
import game.data.AbstractGameData;
import game.data.FileGameData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.junit.Assert.*;

/**
 * This class performs junit test of the configuration mechansm used by the game core
 */
public class SigmoidClassifierTest {
    private static final String datafilename = "data/iris.txt";
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {

        data = new FileGameData(datafilename);

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);


        SigmoidModelConfig smci;
        smci = new SigmoidModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sigmoid classifier");

        int outputs = data.getONumber(); //number of output attributes (classes)
        clc.setModelsNumber(outputs);

        ConfigurationFactory.saveConfiguration(clc, "sigmoid.cfg");

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration("sigmoid.cfg");
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
            //System.out.println("props[out|target]:" + outs + "class predicted:" + ((ConnectableClassifier) c).getOutput());
            for (int i = 0; i < data.getONumber(); i++) {
                err += Math.pow(out[i] - data.getTargetOutput(i), 2);

            }
        }
        System.out.println("Overall RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
    }

    @Test
    public void CountClassDistibutions() {

        DecimalFormat formater = new DecimalFormat("##.###");
        int onum = data.getONumber();
        double[] sums;
        sums = new double[onum];
        for (int k = 0; k < onum; k++) {
            for (int i = 0; i < onum; i++) sums[i] = 0;
            for (int j = 0; j < data.getInstanceNumber(); j++) {
                data.publishVector(j);
                double[] out = ((ConnectableClassifier) c).getOutputProbabilities();
                if (data.getTargetOutput(k) == 1)
                    for (int i = 0; i < onum; i++) {
                        sums[i] += out[i];

                    }
            }
            String ou = "Distributions for class" + k + ": ";
            for (int i = 0; i < onum; i++) {
                ou += sums[i] + " ";//formater.format(sums[i])+" ";

            }
            System.out.println(ou);
        }

    }
}