package game.models;

import configuration.models.single.neural.BackPropagationModelConfig;
import configuration.models.single.neural.CascadeCorrelationModelConfig;
import configuration.models.single.neural.QuickpropModelConfig;
import configuration.models.single.neural.RpropModelConfig;
import game.data.FileGameData;
import game.data.GameData;
import game.models.single.neural.QuickpropModel;
import game.models.single.neural.RpropModel;
import org.junit.Test;

public class NeuralModelTest {

    public static final String DATA_FILE = "c:\\Users\\gias\\Documents\\skola\\5semester\\PROBAP\\testdata\\block.txt";

    static GameData data;
    static ConnectableModel model;
    private static QuickpropModelConfig neural;


    @Test
    public void generateModelConfig() {
        neural = new QuickpropModelConfig();
    }

    @Test
    public void testSetConfig() {
        //  neural.setMaxLayersNumber(10);
        //  neural.setAcceptableError(0.2);
        neural.setTrainingCycles(3000);
    }

    @Test
    public void generateModel() {
        data = new FileGameData(DATA_FILE);
        model = ModelFactory.createNewConnectableModel(neural, data, true);

    }

    @Test
    public void testModels() {
        for (int i = 0; i < data.getONumber(); i++) {
            System.out.println(model.toEquation());
            double err = 0;
            for (int j = 0; j < data.getInstanceNumber(); j++) {
                data.publishVector(j);
                err += Math.pow(model.getOutput() - data.getTargetOutput(i), 2);
                System.out.println(model.getOutput() + ";" + data.getTargetOutput(i));
            }
            System.out.println("RMS Error: " + Math.sqrt(err / data.getInstanceNumber()));
        }
    }
}

