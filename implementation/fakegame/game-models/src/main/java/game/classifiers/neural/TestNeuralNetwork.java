package game.classifiers.neural;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Administrator
 */
public class TestNeuralNetwork {

    public void testAnd() {
        TrainingSet trainingSet = new TrainingSet();
        double[] input = {0, 0};
        double[] desiredOutput = {0};
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 0;
        input[1] = 1;
        desiredOutput[0] = 0;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 0;
        desiredOutput[0] = 0;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 1;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        try {
            NeuralNetwork network = new NeuralNetwork(2, 1, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);
            CascadeCorrelation correlation = new CascadeCorrelation(network);
            correlation.trainNetwork(trainingSet);
            network.printError(trainingSet);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }

    public NeuralNetwork testOr() {
        TrainingSet trainingSet = new TrainingSet();

        double[] input = {0, 0};
        double[] desiredOutput = {0};
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 0;
        input[1] = 1;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 0;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 1;
        desiredOutput[0] = 1;

        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        NeuralNetwork network = null;

        try {
            network = new NeuralNetwork(2, 1, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);

            CascadeCorrelation correlation = new CascadeCorrelation(network);
            correlation.setQuickParams();
            correlation.trainNetwork(trainingSet);
            return network;
        
        /*
        QuickPropagation quick = new QuickPropagation(network);
        SlopeCalcParams info = new SlopeCalcParams();
            info.mode = TrainMode.minimize;
            info.neuralNetwork = network;
            info.synapsesToTrain = network.synapses();
            info.decay = -0.0001;
        for (int i = 0; i <40; i ++){
            quick.train(trainingSet, info, new SlopeCalcFunctionQuickProp());
            network.printError(trainingSet);
        }
        */

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return network;
        /*
        
        try{
            NeuralNetwork network = new NeuralNetwork(2,1,1, new ActivationFunctionSymmetricSigmoid(),true);
            network.printNetwork("network.txt");
            BackPropagation alg = new BackPropagation(network);
            QuickPropagation quick = new QuickPropagation(network);
            RProp rprop = new RProp(network);
            SlopeCalcParams info = new SlopeCalcParams();
            info.mode = TrainMode.minimize;
            info.neuralNetwork = network;
            info.synapsesToTrain = network.synapses();
            for (int i = 0; i < 20; i++){
                network.printNetwork("newtest.txt");
                //alg.train(trainingSet, info, new SlopeCalcFunctionBackProp());
                //quick.train(trainingSet, info, new SlopeCalcFunctionQuickProp());
                rprop.train(trainingSet, info, new SlopeCalcFunctionBackProp());
                //double error = network.calculateNetworkSumSquareError(trainingSet);
                //System.out.println(error);
                network.printError(trainingSet);

            }
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }
         */
    }

    public void testHousing() {

    }


    public void testXor2() {
        TrainingSet trainingSet = new TrainingSet();
        double[] input = {0, 0};
        double[] desiredOutput = {0};
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 0;
        input[1] = 1;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 0;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 1;
        desiredOutput[0] = 0;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        try {
            NeuralNetwork network = new NeuralNetwork(2, 1, 1, new ActivationFunctionSigmoidFahlmanOffset(), false);
            NeuronLayer hiddenLayer = network.createLayer(1, LayerType.hidden, network.neuronId, 0, new ActivationFunctionSigmoidFahlmanOffset());
            network.addHiddenLayer(hiddenLayer, 1);
            network.fullyConnectLayers(network.layers().get(0), network.layers().get(1), true);
            network.fullyConnectLayers(network.layers().get(1), network.layers().get(2), true);
            network.fullyConnectLayers(network.layers().get(0), network.layers().get(2), true);
            BackPropagation back = new BackPropagation(network);
            QuickPropagation quick = new QuickPropagation(network);
            RProp rprop = new RProp(network);
            SlopeCalcParams info = new SlopeCalcParams();
            info.mode = TrainMode.minimize;
            info.neuralNetwork = network;
            info.synapsesToTrain = network.synapses();
            network.printNetworkToFile("network.txt", true);

            for (int i = 0; i < 1000; i++) {
                network.printNetworkToFile("newtest.txt", true);
                back.train(trainingSet, info, new SlopeCalcFunctionBackProp());
                //quick.train(trainingSet, info, new SlopeCalcFunctionQuickProp());
                //back.train(trainingSet, info, new SlopeCalcFunctionBackProp());
                network.printError(trainingSet);
            }
            double error = network.calculateSquaredError(trainingSet);
            double[][] outputs = network.extractOutput(trainingSet, network.outputLayer().size());
            System.out.println(error);


            //CascadeCorrelation correlation = new CascadeCorrelation(network);
            //correlation.trainNetwork(trainingSet);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }


    public NeuralNetwork testXor() {
        TrainingSet trainingSet = new TrainingSet();
        double[] input = {0, 0};
        double[] desiredOutput = {0};
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 0;
        input[1] = 1;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 0;
        desiredOutput[0] = 1;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        input[0] = 1;
        input[1] = 1;
        desiredOutput[0] = 0;
        trainingSet.addTrainingPattern(new TrainingPattern(input, desiredOutput));
        try {
            NeuralNetwork network = new NeuralNetwork(2, 1, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);
            CascadeCorrelation correlation = new CascadeCorrelation(network);
            //correlation.setQuickParams();
            correlation.setRpropParams();
            correlation.trainNetwork(trainingSet);
            return network;
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return null;

    }

    public void makeSpiralData() throws IOException {

        int i;
        double x, y, angle, radius;
        BufferedWriter out = new BufferedWriter(new FileWriter("spiralData.txt", false));
        out.write("inputs 2");
        out.newLine();
        out.write("outputs 1");
        out.newLine();
        double factor = 0.5;
      /* write spiral of data */
        for (i = 0; i <= 96; i++) {
            angle = i * Math.PI / 16.0;
            radius = factor * (104 - i) / 104.0;
            x = radius * Math.sin(angle);
            y = radius * Math.cos(angle);
            out.write(x + " ");
            out.write(y + " ");
            out.write(1.0 + "");
            out.newLine();
            //printf("((%8.5f  %8.5f)   (%3.1f))\n",  x,  y, 1.0);
            //printf("((%8.5f  %8.5f)   (%3.1f))\n", -x, -y, 0.0);
        }
        for (i = 0; i <= 96; i++) {
            angle = i * Math.PI / 16.0;
            radius = factor * (104 - i) / 104.0;
            x = -radius * Math.sin(angle);
            y = -radius * Math.cos(angle);
            out.write(x + " ");
            out.write(y + " ");
            out.write(0.0 + "");
            out.newLine();
        }
        out.close();


    }

    public void showSpiral() throws IOException {
        String inputFile = "spiralData.txt";
        //SpiralInputReader inputReader = new SpiralInputReader (inputFile);
        InputFileReader reader = new InputFileReader(inputFile);
        try {
            TrainingSet trainingSet = reader.extractInputFile(" ", false);
            //TrainingSet trainingSet = inputReader.extractInputFile(" ");

            //SpiralVisualisation vision = new SpiralVisualisation(trainingSet);
            //      vision.setVisible(true);
        } catch (Exception e) {
        }
    }

    public NeuralNetwork testSpiral(String inputFile) {
        try {
            InputFileReader inputReader = new InputFileReader(inputFile);
            TrainingSet trainingSet = inputReader.extractInputFile(" ", false);
            int inputsNumber = trainingSet.getTrainingPattern(0).inputsNumber();
            int outputsNumber = trainingSet.getTrainingPattern(0).desiredOutputsNumber();
            NeuralNetwork network = new NeuralNetwork(inputsNumber, outputsNumber, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);
            CascadeCorrelation cascadeCorrelation = new CascadeCorrelation(network);
            //cascadeCorrelation.setRpropParams();
            cascadeCorrelation.setQuickParams();
            cascadeCorrelation.trainNetwork(trainingSet);
            return network;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;

    }

    public void testHousing(String inputFile) throws Exception {
        try {
            InputFileReader reader = new InputFileReader(inputFile);
            TrainingSet trainingSet = reader.extractInputFile(" ", true);
            this.writeTrainingSetToFile("normalizedHousing.data", trainingSet);
            int inputsNumber = trainingSet.getTrainingPattern(0).inputsNumber();
            int outputsNumber = trainingSet.getTrainingPattern(0).desiredOutputsNumber();
            NeuralNetwork network = new NeuralNetwork(inputsNumber, outputsNumber, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);
            CascadeCorrelation cascadeCorrelation = new CascadeCorrelation(network);
            cascadeCorrelation.trainNetwork(trainingSet);


        } catch (Exception ex) {
            throw new Exception("TestNeuralNetwork: testHousing -> " + ex.getMessage());
        }
    }

    public void writeTrainingSetToFile(String fileName, TrainingSet trainingSet) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, false));
            for (int i = 0; i < trainingSet.size(); i++) {
                TrainingPattern trainingPattern = trainingSet.getTrainingPattern(i);
                for (int k = 0; k < trainingPattern.inputsNumber(); k++) {
                    out.write(trainingPattern.getInput(k) + " ");
                }
                for (int k = 0; k < trainingPattern.desiredOutputsNumber(); k++) {
                    out.write(trainingPattern.getDesiredOutput(k) + " ");
                }
                out.newLine();
            }
            out.close();
        } catch (Exception e) {
        }
    }

}
