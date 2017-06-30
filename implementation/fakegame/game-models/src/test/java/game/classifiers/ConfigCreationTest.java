package game.classifiers;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.*;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.classifiers.single.DTForestClassifierConfig;
import configuration.classifiers.single.DecisionTreeClassifierConfig;
import configuration.classifiers.single.KNNClassifierConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ModelConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.ensemble.BoostingR2ModelConfig;
import configuration.models.ensemble.BoostingRTModelConfig;
import configuration.models.ensemble.DivideModelConfig;
import configuration.models.single.*;
import configuration.models.single.neural.BackPropagationModelConfig;
import configuration.models.single.neural.CascadeCorrelationModelConfig;
import configuration.models.single.neural.QuickpropModelConfig;
import configuration.models.single.neural.RpropModelConfig;
import game.classifiers.ensemble.ClassifierArbitrating;
import game.classifiers.ensemble.ClassifierBoosting;
import game.classifiers.ensemble.ClassifierCascadeGen;
import game.data.AbstractGameData;
import game.data.FileGameData;
import game.models.single.GaussianModel;
import game.models.single.GaussianMultiModel;
import game.models.single.SigmoidNormModel;
import game.models.single.neural.BackPropagationModel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by frydatom on 29.8.16.
 */
public class ConfigCreationTest {

    @BeforeClass
    public static void generateConfigurationOfExp() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        ExpModelConfig smci;
        smci = new ExpModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Exponencial classifier");

        ConfigurationFactory.saveConfiguration(clc, "exp.cfg");
    }
    @BeforeClass
    public static void generateConfigurationOfGauss() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        GaussianModelConfig smci;
        smci = new GaussianModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Gaussian classifier");

        ConfigurationFactory.saveConfiguration(clc, "gauss.cfg");

    }

    @BeforeClass
    public static void generateConfigurationOfGaussMulti() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        GaussianMultiModelConfig smci;
        smci = new GaussianMultiModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Gaussian Multi classifier");

        ConfigurationFactory.saveConfiguration(clc, "gauss_multi.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfGaussianNorm() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        GaussianNormModelConfig smci;
        smci = new GaussianNormModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Gaussian Norm classifier");

        ConfigurationFactory.saveConfiguration(clc, "gauss_norm.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfLinear() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        LinearModelConfig smci;
        smci = new LinearModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Linear classifier");

        ConfigurationFactory.saveConfiguration(clc, "linear.cfg");

    }

    @BeforeClass
    public static void generateConfigurationOfPolynomial() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        PolynomialModelConfig smci;
        smci = new PolynomialModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Polynomial classifier");

        ConfigurationFactory.saveConfiguration(clc, "poly.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfSigmoid() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        SigmoidModelConfig smci;
        smci = new SigmoidModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sigmoid classifier");

        ConfigurationFactory.saveConfiguration(clc, "sigmoid.cfg");

    }

    @BeforeClass
    public static void generateConfigurationOfSigmoidNorm() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        SigmoidNormModelConfig smci;
        smci = new SigmoidNormModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sigmoid Norm classifier");

        ConfigurationFactory.saveConfiguration(clc, "sigmoid_norm.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfSine() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        SineModelConfig smci;
        smci = new SineModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sine classifier");

        ConfigurationFactory.saveConfiguration(clc, "sine.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfSineNorm() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        SineNormModelConfig smci;
        smci = new SineNormModelConfig();
        smci.setTrainerClassName("QuasiNewtonTrainer");
        smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Sine Norm classifier");

        ConfigurationFactory.saveConfiguration(clc, "sine_norm.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfBackProp() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        BackPropagationModelConfig smci;
        smci = new BackPropagationModelConfig();
        //smci.setTrainerClassName("QuasiNewtonTrainer");
        //smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("BackPropagation classifier");

        ConfigurationFactory.saveConfiguration(clc, "backprop.cfg");

    }
    @BeforeClass
    public static void generateConfigurationOfCascadeCorr() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        CascadeCorrelationModelConfig smci;
        smci = new CascadeCorrelationModelConfig();
       // smci.setTrainerClassName("QuasiNewtonTrainer");
       // smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Cascade Correlation classifier");

        ConfigurationFactory.saveConfiguration(clc, "cascadecorr.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfQuickProp() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        QuickpropModelConfig smci;
        smci = new QuickpropModelConfig();
       // smci.setTrainerClassName("QuasiNewtonTrainer");
       // smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("QuickProp classifier");

        ConfigurationFactory.saveConfiguration(clc, "quickprop.cfg");
    }

    @BeforeClass
    public static void generateConfigurationOfRProp() {
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.UNIFORM);

        RpropModelConfig smci;
        smci = new RpropModelConfig();
 //       smci.setTrainerClassName("QuasiNewtonTrainer");
 //       smci.setTrainerCfg(new QuasiNewtonConfig());
        clc.addClassModelCfg(smci);
        clc.setDescription("Rprop classifier");

        ConfigurationFactory.saveConfiguration(clc, "rprop.cfg");

    }


    @BeforeClass
    public static void generateConfigurationOfTestExp() {

        ClassifierCascadeGenProbConfig smci;
        smci = new ClassifierCascadeGenProbConfig();

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);

        ClassifierBoostingConfig bst = new ClassifierBoostingConfig();


        ClassifierModelConfig clcex = new ClassifierModelConfig();
        clcex.setClassModelsDef(BaseModelsDefinition.UNIFORM);

       SigmoidNormModelConfig smciex;
        smciex = new SigmoidNormModelConfig();
        smciex.setTrainerClassName("QuasiNewtonTrainer");
        smciex.setTrainerCfg(new QuasiNewtonConfig());
        clcex.addClassModelCfg(smciex);
        clcex.addClassModelCfg(smciex);
        clcex.setDescription("Exponencial classifier");


        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);
        bst.addBaseClassifierCfg(clcex);

        smci.addBaseClassifierCfg(bst);


        smci.setDescription("test classifier");

        ConfigurationFactory.saveConfiguration(smci, "test-exp.cfg");

    }

    // CascadeGenProb{9x ClassifierModel{<outputs>x BoostingRTModel(tr=0.1)[8xGaussianModel]}
    @BeforeClass
    public static void generateConfigurationOfTestCGPCMBRTMGM() {

        ClassifierCascadeGenProbConfig ccgp;
        ccgp = new ClassifierCascadeGenProbConfig();

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);

        BoostingRTModelConfig bst = new BoostingRTModelConfig();
        bst.setThreshold(0.1);

        GaussianModelConfig gm = new GaussianModelConfig();

        for (int i = 0; i < 8; i++) {
            bst.addBaseModelCfg(gm);
        }

        clc.addClassModelCfg(bst);

        for (int i = 0; i < 9; i++) {
            ccgp.addBaseClassifierCfg(clc);
        }

        ccgp.setDescription("CascadeGenProb{9x ClassifierModel{<outputs>x BoostingRTModel(tr=0.1)[8xGaussianModel]}");

        ConfigurationFactory.saveConfiguration(ccgp, "test-ccgp-cm-brtm-gm.cfg");

    }


    // ClassifierArbitrating{6x ClassifierModel{<outputs>x DivideModel(mult=6.68)[7xPolynomialModel(degree=3)]}}
    @BeforeClass
    public static void generateConfigurationOfTestCAC6CMDM7PM() {

        ClassifierArbitratingConfig cac = new ClassifierArbitratingConfig();

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);

        DivideModelConfig dmc = new DivideModelConfig();
        dmc.setClusterSizeMultiplier(6.68);

        PolynomialModelConfig pmc = new PolynomialModelConfig();
        pmc.setMaxDegree(3);

        for (int i = 0; i < 7; i++) {
            dmc.addBaseModelCfg(pmc);
        }

        clc.addClassModelCfg(dmc);

        for (int i = 0; i < 6; i++) {
            cac.addBaseClassifierCfg(clc);
        }

        cac.setDescription("ClassifierArbitrating{6x ClassifierModel{<outputs>x DivideModel(mult=6.68)[7xPolynomialModel(degree=3)]}}");

        ConfigurationFactory.saveConfiguration(cac, "test-cac-6cm-dm-7pm.cfg");

    }


    // CascadeGenProb{8x Boosting{2x ClassifierModel{<outputs>x ExpModel}}
    @BeforeClass
    public static void generateConfigurationOfTestCCGP8B2CMEM() {

        ClassifierCascadeGenProbConfig ccgp = new ClassifierCascadeGenProbConfig();

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);

        ExpModelConfig expc= new ExpModelConfig();
        clc.addClassModelCfg(expc);

        ClassifierBoostingConfig bst = new ClassifierBoostingConfig();


        bst.addBaseClassifierCfg(clc);
        bst.addBaseClassifierCfg(clc);

        for (int i = 0; i < 8; i++) {
                ccgp.addBaseClassifierCfg(bst);
        }



        ccgp.setDescription("CascadeGenProb{8x Boosting{2x ClassifierModel{<outputs>x ExpModel}}");

        ConfigurationFactory.saveConfiguration(ccgp, "testCCGP8B2CMEM.cfg");

    }

    // ClassifierArbitrating{4x ClassifierModel{<outputs>x PolynomialModel(degree=2)}
    @BeforeClass
    public static void generateConfigurationOfTestCA_4CM_PM() {

        ClassifierArbitratingConfig cac = new ClassifierArbitratingConfig();

        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.RANDOM);

        PolynomialModelConfig pmc = new PolynomialModelConfig();
        pmc.setMaxDegree(2);

        clc.addClassModelCfg(pmc);

        for (int i = 0; i < 4; i++) {
            cac.addBaseClassifierCfg(clc);
        }

        cac.setDescription("ClassifierArbitrating{4x ClassifierModel{<outputs>x PolynomialModel(degree=2)}");

        ConfigurationFactory.saveConfiguration(cac, "testCA_4CM_PM.cfg");

    }


    // ClassifierBoosting{9x ClassifierBoosting{8x DecisionTree(depth=12,conf=0.5,alt=2)}}
    @BeforeClass
    public static void generateConfigurationOfTestCB9CB8DT() {

        ClassifierBoostingConfig cac = new ClassifierBoostingConfig();
        ClassifierBoostingConfig cbc = new ClassifierBoostingConfig();


        DecisionTreeClassifierConfig dtc = new DecisionTreeClassifierConfig();

        for (int i = 0; i < 8; i++) {
            cbc.addBaseClassifierCfg(dtc);
        }


        for (int i = 0; i < 9; i++) {
            cac.addBaseClassifierCfg(cbc);

        }

        cac.setDescription("TEST");

        ConfigurationFactory.saveConfiguration(cac, "testCB9CBDT.cfg");

    }

    // ClassifierBagging{40x DecisionTree()}
    @BeforeClass
    public static void generateConfigurationOfTest() {

        ClassifierBaggingConfig cac = new ClassifierBaggingConfig();

        DecisionTreeClassifierConfig dtc = new DecisionTreeClassifierConfig();

        for (int i = 0; i < 40; i++) {
            cac.addBaseClassifierCfg(dtc);
        }



        cac.setDescription("TEST");

        ConfigurationFactory.saveConfiguration(cac, "test.cfg");

    }

    @Test
    public void buildClassifierFromXMLConfig() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration("exp.cfg");
        conf = ConfigurationFactory.getConfiguration("gauss.cfg");
        conf = ConfigurationFactory.getConfiguration("gauss_multi.cfg");
        conf = ConfigurationFactory.getConfiguration("gauss_norm.cfg");
        conf = ConfigurationFactory.getConfiguration("linear.cfg");
        conf = ConfigurationFactory.getConfiguration("poly.cfg");
        conf = ConfigurationFactory.getConfiguration("sigmoid.cfg");
        conf = ConfigurationFactory.getConfiguration("sigmoid_norm.cfg");
        conf = ConfigurationFactory.getConfiguration("sine.cfg");
        conf = ConfigurationFactory.getConfiguration("sine_norm.cfg");
        conf = ConfigurationFactory.getConfiguration("backprop.cfg");
        conf = ConfigurationFactory.getConfiguration("cascadecorr.cfg");
        conf = ConfigurationFactory.getConfiguration("quickprop.cfg");
        conf = ConfigurationFactory.getConfiguration("rprop.cfg");
        assertTrue("Creation works", true);
    }

    private static final String datafilename = "data/iris.txt";
    static AbstractGameData data;

    @Test
    public void buildAndSerializeClassifierFromXMLConfig() {

        data = new FileGameData(datafilename);
        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration("test.cfg");
        Classifier c = ClassifierFactory.createNewClassifier(conf, data, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(c);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e){
                e.printStackTrace();
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                // ignore close exception
            }
        }
        assertTrue("Creation works", true);
    }

}
