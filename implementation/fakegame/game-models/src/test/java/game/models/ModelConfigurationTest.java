package game.models;

import game.data.FileGameData;
import game.data.GameData;

import org.junit.Test;

import configuration.game.trainers.QuasiNewtonConfig;
import configuration.models.single.PolynomialModelConfig;

public class ModelConfigurationTest {
    public static final String DATA_FILE = "data/iris.txt";


    private static PolynomialModelConfig pmc;

    @Test
    public void generateModelConfig() {
        pmc = new PolynomialModelConfig();
    }

    @Test
    public void testSetConfig() {
        if(pmc==null)generateModelConfig();
        pmc.setTrainerClassName("QuasiNewtonTrainer");
        pmc.setTrainerCfg(new QuasiNewtonConfig());    // trained by LMS, in case of singular matrix QN is used
        pmc.setMaxDegree(5);
    }

    @Test
    public void generateModel() {
        //pmc.setClassRef(PolynomialModel.class);
        if(pmc==null)generateModelConfig();
        GameData gameData = new FileGameData(DATA_FILE);
        ModelLearnable model = ModelFactory.createNewModel(pmc, gameData, true);
    }

    @Test
    public void generateConnectableModel() {
        //pmc.setClassRef(PolynomialModel.class);
        if(pmc==null)generateModelConfig();
        GameData gameData = new FileGameData(DATA_FILE);
        ConnectableModel model = ModelFactory.createNewConnectableModel(pmc, gameData, true);

    }


}
