package game.models.evolution;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.evolution.TreeEvolutionModelConfig;
import configuration.models.single.LinearModelConfig;
import game.classifiers.Classifier;
import game.classifiers.ClassifierFactory;
import game.classifiers.ConnectableClassifier;
import game.data.AbstractGameData;
import game.data.FileGameData;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by frydatom on 23.9.16.
 */
public class TreeEvolutionModelTest {
    private static final String datafilename = "data/iris.txt";
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {
        // Setup logger
        Properties p = new Properties();
        p.setProperty("log4j.rootLogger", "INFO, A1");
        p.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        p.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        p.setProperty("log4j.appender.A1.layout.ConversionPattern", "%d{ABSOLUTE};%m%n");
        PropertyConfigurator.configure(p);


        data = new FileGameData(datafilename);

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);


        TreeEvolutionModelConfig smci;
        smci = new TreeEvolutionModelConfig();
        smci.setComputationTime(60);
        clc.addClassModelCfg(smci);
        clc.setDescription("Tree Evolution Model");

        int outputs = data.getONumber(); //number of output attributes (classes)
        clc.setModelsNumber(outputs);

        ConfigurationFactory.saveConfiguration(clc, "tree_evolution.cfg");

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration("tree_evolution.cfg");

        c = ClassifierFactory.createNewClassifier(conf, data, true);
        assertTrue("Creation works", true);
    }


    @Test
    public void evaluateClassifier() {
//        System.out.println(((ConnectableClassifier) c).toEquation());
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
            //  System.out.println("props[out|target]:" + outs + "class predicted:" + ((ConnectableClassifier) c).getOutput());
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