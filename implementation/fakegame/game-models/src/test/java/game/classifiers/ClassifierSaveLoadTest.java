package game.classifiers;

import configuration.CfgTemplate;
import game.data.FileGameData;
import game.data.GameData;
import game.models.ModelLearnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import configuration.ConfigurationFactory;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.ensemble.BaseModelsDefinition;
import configuration.models.single.PolynomialModelConfig;

/**
 * This class performs junit test of the configuration mechansm used by the ame core
 */
public class ClassifierSaveLoadTest {
    private static final String cfgfilename = "cfg/classifier_poly2.properties";
    private static final String datafilename = "data/iris.txt";
    static ModelLearnable model;
    static CfgTemplate generatedCfg;
    static GameData data;
    static FileInputStream fis;
    static DataInputStream dis;
    static FileOutputStream fos;
    static DataOutputStream dos;
    static Classifier c;

    @BeforeClass
    public static void generateConfigurationOfClassifierAndLoadData() {
        data = new FileGameData(datafilename);
        int outputs = data.getONumber(); //number of output attributes (classes)
        PolynomialModelConfig[] pmc = new PolynomialModelConfig[outputs];
        ClassifierModelConfig clc = new ClassifierModelConfig();
        clc.setClassModelsDef(BaseModelsDefinition.PREDEFINED);
        for (int i = 0; i < outputs; i++) {
            pmc[i] = new PolynomialModelConfig();
            pmc[i].setTrainerClassName("QuasiNewtonTrainer");
            pmc[i].setTrainerCfg(new QuasiNewtonConfig());    // trained by LMS, in case of singular matrix QN is used
            pmc[i].setMaxDegree(3);
            pmc[i].setTargetVariable(i);
            clc.addClassModelCfg(pmc[i]);
        }   // a polynomial neuron for each output
        generatedCfg = clc;
        ConfigurationFactory.saveConfiguration(generatedCfg, cfgfilename);


    }

    @Test
    public void testConfigurationOfClassifiers() {

        CfgTemplate conf;
        conf = ConfigurationFactory.getConfiguration(cfgfilename);
        c = ClassifierFactory.createNewClassifier(conf, data, true);
    }

    @Test
    public void saveClassifiers() {
        try {
            fos = new FileOutputStream("iris-classify-poly3.net");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        dos = new DataOutputStream(fos);
        ArrayList<Classifier> m = new ArrayList<Classifier>();
        Classifier[] mo = Classifiers.getInstance().getClassifiersArray();
        for (int i = 0; i < mo.length; i++)
            m.add(mo[i]);

        Classifiers.saveClassifiersToXMLStream(dos, m);
        try {
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Test
    public void loadModels() {
        try {
            fis = new FileInputStream("iris-classify-poly3.net");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        dis = new DataInputStream(fis);
        ArrayList<Classifier> c = Classifiers.loadClassifiersfromXMLStream(dis);
        for (int i = 0; i < c.size(); i++)
            Classifiers.getInstance().storeClassifierAt(c.get(i), i);
    }
}