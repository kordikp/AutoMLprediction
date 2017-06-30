package game.classifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import configuration.CfgTemplate;
import configuration.models.single.*;
import game.classifiers.single.ClassifierModel;
import game.data.AbstractGameData;
import game.data.FileGameData;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

/**
 * This class performs junit test of the configuration mechansm used by the game core
 */
public class ClassifierTest {
    private static final String datafilename = "data/iris.txt";
    static AbstractGameData data;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfModelsAndLoadData() {

        data = new FileGameData(datafilename);

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

/*
         LinearModelConfig smci;
        smci = new LinearModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                   smci.setRetrainWhenLmsFails(true);
                    clc.addClassModelCfg(smci);
                clc.setDescription("Linear Class Separation");
        */

        PolynomialModelConfig smci;
        smci = new PolynomialModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        smci.setMaxDegree(5);
        clc.addClassModelCfg(smci);

        clc.setDescription("Polynomial classifier with max degree 5");

         /*
        SigmoidModelConfig smci;
        smci = new SigmoidModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sigmoid classifier");
         */
/*
        SigmoidNormModelConfig smci;
        smci = new SigmoidNormModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                       clc.addClassModelCfg(smci);
        clc.setDescription("Sigmoid classifier (without scaling and shift coefficients)");
            */
        /* GaussianModelConfig smci;
       smci = new GaussianModelConfig();
                  smci.setTrainerClassName("QuasiNewtonTrainer");
                  smci.setTrainerCfg(new QuasiNewtonConfig());
                      clc.addClassModelCfg(smci);
       clc.setDescription("Gaussian classifier (each class single peak");
        */
        /*
        SineModelConfig smci;
               smci = new SineModelConfig();
                          smci.setTrainerClassName("QuasiNewtonTrainer");
                          smci.setTrainerCfg(new QuasiNewtonConfig());
                              clc.addClassModelCfg(smci);
               clc.setDescription("Sine classifier (each class separate periodic occurrence");
 */
        /*
        GaussianMultiModelConfig smci;
        smci = new GaussianMultiModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                       clc.addClassModelCfg(smci);
        clc.setDescription("Gaussian with fractal behaviour classifier");
        int outputs = data.getONumber(); //number of output attributes (classes)
        clc.setModelsNumber(outputs);
        */

        /*
        GaussianNormModelConfig smci;
        smci = new GaussianNormModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                       clc.addClassModelCfg(smci);
        clc.setDescription("Gaussian without scaling and shift");
        */

        /*SineNormModelConfig smci;
        smci = new SineNormModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                       clc.addClassModelCfg(smci);
        clc.setDescription("Sine without scaling and shift classifier");
        */
        /*ExpModelConfig smci;
        smci = new ExpModelConfig();
                   smci.setTrainerClassName("QuasiNewtonTrainer");
                   smci.setTrainerCfg(new QuasiNewtonConfig());
                       clc.addClassModelCfg(smci);
        clc.setDescription("Exponencial classifier");
        */
        int outputs = data.getONumber(); //number of output attributes (classes)
        clc.setModelsNumber(outputs);

        ConfigurationFactory.saveConfiguration(clc, "poly.cfg");

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration("poly.cfg");
        c = ClassifierFactory.createNewClassifier(conf, data, true);
        /*
        try {

            FileOutputStream fileOut =
                    new FileOutputStream("/tmp/poly.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(c);
            out.close();
            fileOut.close();
        }
        catch(Throwable t){
            t.printStackTrace();

        }*/
        assertTrue("Creation works", true);
    }

    @Test
    public void evaluateClassifier() {
        if(c==null)buildClassifierFromXMLConfig();
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

    @Test
    public void CountClassDistibutions() {
        if(c==null)buildClassifierFromXMLConfig();
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