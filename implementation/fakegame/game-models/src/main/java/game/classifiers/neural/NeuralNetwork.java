/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Do Minh Duc
 */
public class NeuralNetwork implements Serializable {
    /* VARIABLES DECLARATIONS */
    private ArrayList<NeuronLayer> neuronLayers;
    private transient BufferedWriter out;
    public int neuronId;

    /*===================== PUBLIC METHODS ============================*/

    /**
     * Neural network constructor.
     *
     * @param inputsNumber       number of inputs
     * @param outputsNumber      number of outputs
     * @param biasNumber         number of bias neurons
     * @param activationFunction activation function of neurons
     * @param connect            if true automatically connect input neurons with output neurons
     * @throws java.lang.Exception
     */
    public NeuralNetwork(int inputsNumber, int outputsNumber, int biasNumber, IActivationFunction activationFunction, boolean connect) throws Exception {
        try {
            this.neuronId = 0;
            this.neuronLayers = new ArrayList<NeuronLayer>();
            NeuronLayer inputLayer = createLayer(inputsNumber, LayerType.input, this.neuronId, biasNumber, new ActivationFunctionLinear());
            NeuronLayer outputLayer = createLayer(outputsNumber, LayerType.output, this.neuronId, 0, activationFunction);
            if (connect) {
                this.fullyConnectLayers(inputLayer, outputLayer, true);
            }
            this.neuronLayers.add(inputLayer);
            this.neuronLayers.add(outputLayer);
        } catch (Exception ex) {
            throw new Exception("NeuralNetwork: NeuralNetwork -> " + ex.getMessage());
        }
    }

    /**
     * Creates a neuron layer of specific property
     *
     * @param neuronsNumber      number of neurons in layer
     * @param layerType          type of layer
     * @param neuronId           just for debugging, should be removed at final version
     * @param biasNumber         number of bias neurons
     * @param activationFunction activation function of neurons
     * @return newly created neuron layer
     */
    public NeuronLayer createLayer(int neuronsNumber, LayerType layerType, int neuronId, int biasNumber, IActivationFunction activationFunction) throws Exception {
        NeuronLayer newLayer = new NeuronLayer(neuronsNumber, layerType, neuronId, biasNumber, activationFunction);
        this.neuronId += neuronsNumber + biasNumber;
        return newLayer;

    }

    /**
     * Connects neurons of one layer with neurons of other layer.
     *
     * @param layerFrom    neuron layer where synapse connections originate
     * @param layerTo      neuron layer which synapse connections enter
     * @param randomWeight if true synapse weight are randomly generated
     * @throws java.lang.Exception
     */
    public void fullyConnectLayers(NeuronLayer layerFrom, NeuronLayer layerTo, boolean randomWeight) throws Exception {
        Iterator<Neuron> fromNeuronsIterator = layerFrom.neuronList().iterator();
        while (fromNeuronsIterator.hasNext()) {
            Neuron fromNeuron = fromNeuronsIterator.next();
            Iterator<Neuron> toNeuronsIterator = layerTo.neuronList().iterator();
            while (toNeuronsIterator.hasNext()) {
                Neuron toNeuron = toNeuronsIterator.next();
                this.connectNeurons(fromNeuron, toNeuron, randomWeight);
            }
        }
    }

    /**
     * Adds new hidden layer to neural network at specific position.
     *
     * @param layer neuron layer to be added
     * @param index position where neuron layer will be added
     * @return true if processed without error
     */
    public boolean addHiddenLayer(NeuronLayer layer, int index) {
        if (index < 1 || index >= this.neuronLayers.size()) return false;
        if (layer.type() == LayerType.input || layer.type() == LayerType.output) return false;
        this.neuronLayers.add(index, layer);
        return true;
    }

    /**
     * Adds new hidden layer to neural network at specific position
     *
     * @param neuronsNumber      number of neurons
     * @param activationFunction activation function
     * @param index              position where neuron layer will be added
     * @return true if processed without error
     * @throws java.lang.Exception
     */
    public boolean addHiddenLayer(int neuronsNumber, IActivationFunction activationFunction, int index) {
        if (index < 1 || index >= this.neuronLayers.size()) return false;
        try {
            NeuronLayer newHiddenLayer = new NeuronLayer(neuronsNumber, LayerType.hidden, neuronId, 0, activationFunction);
            this.fullyConnectLayers(this.neuronLayers.get(index - 1), newHiddenLayer, true);
            this.fullyConnectLayers(newHiddenLayer, this.neuronLayers.get(index), true);
            this.neuronLayers.add(index, newHiddenLayer);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Adds new hidden layer to neural network at position behind all currently existed hidden layers
     *
     * @param neuronsNumber      number of neurons
     * @param activationFunction activation function
     * @return true if processed without error
     */
    public boolean addHiddenLayer(int neuronsNumber, IActivationFunction activationFunction) {
        return this.addHiddenLayer(neuronsNumber, activationFunction, this.neuronLayers.size() - 1);
    }

    /**
     * Connects 2 neurons
     *
     * @param sourceNeuron      source neuron
     * @param destinationNeuron destination neuron
     * @param randomWeight      if true weight of synapse connection is generated randomly
     */
    public void connectNeurons(Neuron sourceNeuron, Neuron destinationNeuron, boolean randomWeight) {
        Synapse newSynapse = new Synapse(sourceNeuron, destinationNeuron);
        if (randomWeight) newSynapse.generateWeight();
    }

    /**
     * Connects 2 neurons
     *
     * @param sourceNeuron      source neuron
     * @param destinationNeuron destionation neuron
     * @param weight            weight of synapse connection
     */
    public void connectNeurons(Neuron sourceNeuron, Neuron destinationNeuron, double weight) {
        new Synapse(sourceNeuron, destinationNeuron, weight);
    }

    /**
     * Returns input neuron layer
     *
     * @return input neuron layer
     */
    public NeuronLayer inputLayer() {
        return this.neuronLayers.get(0);
    }

    /**
     * Returns output neuron layer
     *
     * @return output neuron layer
     */
    public NeuronLayer outputLayer() {
        return this.neuronLayers.get(this.neuronLayers.size() - 1);

    }

    /**
     * Returns hidden neuron layers
     *
     * @return arraylist of hidden neuron layers
     */
    public ArrayList<NeuronLayer> hiddenLayers() {
        List temp = this.neuronLayers.subList(1, neuronLayers.size() - 1);
        return new ArrayList(temp);
    }

    /**
     * Returns all neuron layers
     *
     * @return arraylist of all neuron layers
     */
    public ArrayList<NeuronLayer> layers() {
        return this.neuronLayers;
    }

    /**
     * Returns number of neuron layers
     *
     * @return number of neuron layers
     */
    public int size() {
        return this.neuronLayers.size();
    }

    /**
     * Returns all synapse connections
     *
     * @return arraylist of all synapse connections
     */
    public ArrayList<Synapse> synapses() {
        ArrayList<Synapse> synapses = new ArrayList<Synapse>();
        Iterator<NeuronLayer> neuronLayersIterator = this.neuronLayers.iterator();
        while (neuronLayersIterator.hasNext()) {
            Iterator<Neuron> neuronsIterator = neuronLayersIterator.next().neuronList().iterator();
            while (neuronsIterator.hasNext()) {
                synapses.addAll(neuronsIterator.next().incomingSynapses());
            }
        }
        return synapses;
    }


    /**
     * Bubbles the input from inputs through network to outputs
     */
    public void bubbleThrough() {
        ArrayList<NeuronLayer> layers = this.hiddenLayers();
        layers.add(this.outputLayer());
        Iterator<NeuronLayer> layerIterator = layers.iterator();
        while (layerIterator.hasNext()) {
            Iterator<Neuron> neuronIterator = layerIterator.next().neuronList().iterator();
            while (neuronIterator.hasNext()) {
                Neuron neuron = neuronIterator.next();
                neuron.calculateNetInput();
                neuron.calculateOutput();
                neuron.calculateDerivative();
            }
        }
    }


    /**
     * Resets partial derivatives of all synapses by setting to 0
     */
    public void resetSlopes() {
        this.resetSlopes(this.synapses());
    }

    /**
     * Resets partial derivatives of synapses specified in parameter
     *
     * @param synapsesToReset synapses which partial derivatives are to be reset
     */
    public void resetSlopes(ArrayList<Synapse> synapsesToReset) {
        Iterator<Synapse> synapseIterator = synapsesToReset.iterator();
        while (synapseIterator.hasNext()) {
            synapseIterator.next().setCurrentSlope(0);
        }
    }

    /**
     * Resets deltas of all synapses by setting to 0
     */
    public void resetDeltas() {
        Iterator<NeuronLayer> layerIterator = this.neuronLayers.iterator();
        while (layerIterator.hasNext()) {
            Iterator<Neuron> neuronIterator = layerIterator.next().neuronList().iterator();
            while (neuronIterator.hasNext()) {
                neuronIterator.next().setCurrentDelta(0);
            }
        }

    }

    /**
     * Processes inputs in training set and returns correspondent outputs
     *
     * @param trainingSet training set
     * @return array of outputs of all input patterns
     * @throws java.lang.Exception
     */
    public double[][] extractOutput(TrainingSet trainingSet, int numberOfOutputs) throws Exception {
        double[][] outputs;
        outputs = new double[trainingSet.size()][numberOfOutputs];
        Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
        int patternIndex = 0;
        while (patternIterator.hasNext()) {
            TrainingPattern trainingPattern = patternIterator.next();
            outputs[patternIndex] = this.extractOutput(trainingPattern);
            patternIndex++;
        }
        return outputs;
    }

    public double[] extractOutput(TrainingPattern trainingPattern) throws Exception {
        try {
            this.injectInput(trainingPattern.getInputPattern());
        } catch (Exception ex) {
            throw new Exception("NeuralNetwork: extractOutput -> " + ex.getMessage());
        }
        this.bubbleThrough();
        return this.extractOutput();
    }

    public double[] extractOutput() {
        double[] outputs = new double[this.outputLayer().size()];
        Iterator<Neuron> neuronIterator = this.outputLayer().neuronList().iterator();
        int index = 0;
        while (neuronIterator.hasNext()) {
            outputs[index++] = neuronIterator.next().currentOutput();
        }
        return outputs;
    }

    /**
     * Calculates residual error at outputs
     *
     * @param desiredOutput desired values of outputs
     * @param outputIndex
     * @return residual error
     * @throws java.lang.Exception
     */
    public double calculateOutputResidualError(double desiredOutput, int outputIndex) throws Exception {
        Neuron outputNeuron = this.outputLayer().getNeuron(outputIndex);
        double derivative = outputNeuron.calculateDerivative();
        double outputValue = outputNeuron.currentOutput();
        double residualError = (outputValue - desiredOutput) * derivative;
        return residualError;
    }

    /**
     * Calculates sum square error for training pattern
     *
     * @param trainingPattern training pattern
     * @return sum square error
     * @throws java.lang.Exception
     */
    public double calculateSquaredError(TrainingPattern trainingPattern) throws Exception {
        double patternSumSquareError = 0;
        NeuronLayer outputLayer = this.outputLayer();
        this.injectInput(trainingPattern.getInputPattern());
        this.bubbleThrough();
        Pattern desiredOutputs = trainingPattern.getDesiredOutputs();
        if (desiredOutputs.size() != outputLayer.size())
            throw new Exception("NeuralNetwork: calculateNetworkSumSquareError: number of training pattern outputs doesn't match the network");
        else {
            Iterator<Neuron> neuronIterator = outputLayer.neuronList().iterator();
            int index = 0;
            while (neuronIterator.hasNext()) {
                double outputValue = neuronIterator.next().currentOutput();
                //      System.out.println("out:"+outputValue +" target:"+ desiredOutputs.get(index));
                double pom = outputValue - desiredOutputs.get(index++);

                patternSumSquareError += Math.pow(pom, 2);
            }
        }
        return patternSumSquareError;
    }

    /**
     * Calculates sum square error for training set
     *
     * @param trainingSet training set
     * @return sum square error
     * @throws java.lang.Exception
     */
    public double calculateSquaredError(TrainingSet trainingSet) throws Exception {
        double squaredError = 0;
        Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
        while (patternIterator.hasNext()) {
            squaredError += calculateSquaredError(patternIterator.next());
        }
        return squaredError;
    }

    public double calculateMeanSquaredError(TrainingSet trainingSet) throws Exception {
        return this.calculateSquaredError(trainingSet) / trainingSet.size();
    }

    /**
     * stores all last partial derivatives
     */
    public void storeLastSlope() {
        this.storeLastSlope(this.synapses());
    }

    /**
     * stores last partial derivatives of synapses specified in parameter
     *
     * @param synapsesToStore synapses which partial derivatives are to be stored
     */
    public void storeLastSlope(ArrayList<Synapse> synapsesToStore) {
        Iterator<Synapse> synapseIterator = synapsesToStore.iterator();
        while (synapseIterator.hasNext()) {
            synapseIterator.next().storeCurrentSlope();
        }
    }

    /*
     * check if really not needed
    public void makePartialDerivativeAverage(ArrayList<Synapse> synapsesToTrain, int trainingPatternNumber){
        for (int i = 0; i < synapsesToTrain.size(); i++){
            Synapse synapse = synapsesToTrain.get(i);
            synapse.setPartialDerivative(synapse.getPartialDerivative()/trainingPatternNumber);
        }


    }
    */

    /*
     * check if really not needed
    public double calculateNeuronOutputValue(TrainingPattern trainingPattern, Neuron neuron)throws Exception{
        this.injectInput(trainingPattern.getInputPattern());
        this.bubbleOuput();
        return neuron.getCurrentOutputValue();
    }
     * */

    
     



    /*============== PRIVATE METHODS ==================================*/

    /**
     * Checks consistency of neural network
     *
     * @return true if no error found
     */
    private boolean checkNeuralNetworkCorectness() {
        return (checkInputLayer() && checkOutputLayer() && checkHiddenLayers());
    }

    /**
     * Checks consistency of input layer of neural network.
     * Checks if the first layer is an input type layer.
     *
     * @return true if no error found
     */
    private boolean checkInputLayer() {
        if (this.neuronLayers.get(0).type() != LayerType.input) return false;
        else return true;
    }

    /**
     * Checks consistency of output layer of neural network.
     * Checks if the last layer is an output type layer.
     *
     * @return true if no error found
     */
    private boolean checkOutputLayer() {
        if (this.neuronLayers.get(this.neuronLayers.size() - 1).type() != LayerType.output) return false;
        else return true;
    }

    /**
     * Checks consistency of hidden layers of neural network.
     * Checks if all hidden layers are hidden type layers.
     *
     * @return true if no error found
     */
    private boolean checkHiddenLayers() {
        ArrayList<NeuronLayer> hiddenLayers = this.hiddenLayers();
        Iterator<NeuronLayer> layerIterator = hiddenLayers.iterator();
        while (layerIterator.hasNext()) {
            if (!this.checkHiddenLayer(layerIterator.next())) return false;
        }
        return true;
    }

    /**
     * Checks consistency of hidden layer passed in parameter.
     * Checks if the passed layer is hidden type layer.
     *
     * @param hiddenLayer hidden layer to be checked
     * @return true if no error found
     */
    private boolean checkHiddenLayer(NeuronLayer hiddenLayer) {
        if (hiddenLayer.type() != LayerType.hidden) return false;
        else return true;
    }

    /**
     * Checks if number of inputs in training pattern matches number of inputs of neural network.
     *
     * @param pattern input pattern to be checked
     * @return true if number of inputs matches
     */
    private boolean checkPatternInputNumber(Pattern pattern) {
        if (pattern.size() == this.inputLayer().size() - this.inputLayer().biasNumber()) return true;
        else return false;
    }

    /**
     * Injects pattern inputs into network
     *
     * @param inputPattern input pattern
     * @return true if injection was successful
     * @throws java.lang.Exception
     */
    public boolean injectInput(Pattern inputPattern) throws Exception {
        if (!this.checkPatternInputNumber(inputPattern)) return false;
        else {
            NeuronLayer inputLayer = this.inputLayer();
            for (int i = 0; i < inputPattern.size(); i++) {
                try {
                    inputLayer.getNeuron(i).setOutput(inputPattern.get(i));
                } catch (Exception ex) {
                    throw new Exception("NeuralNetwork: injectInput -> " + ex.getMessage());
                }
            }

            return true;
        }
    }


    /**
     * Checks if number of outputs in training pattern matches number of outputs of neural network.
     *
     * @param pattern pattern to be checked
     * @return true if no error found
     */
    private boolean checkPatternOutputNumber(Pattern pattern) {
        if (pattern.size() == this.outputLayer().size()) return true;
        else return false;
    }

    public void printError(TrainingSet trainingSet) throws Exception {
        try {
            System.out.println("E = " + this.calculateSquaredError(trainingSet));
        } catch (Exception ex) {
            throw new Exception("NeuralNetwork: printError -> " + ex.getMessage());
        }
    }

    public void printNetworkToFile(String fileName, boolean append) {
        try {
            FileWriter fstream = new FileWriter(fileName, append);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("number of layers: " + this.neuronLayers.size());
            out.newLine();
            Iterator<NeuronLayer> layerIterator = this.neuronLayers.iterator();
            int index = 0;
            while (layerIterator.hasNext()) {
                NeuronLayer layer = layerIterator.next();
                out.write("layer " + index++ + ":  " + layer.size() + " neurons");
                out.newLine();
                Iterator<Neuron> neuronIterator = layer.neuronList().iterator();
                while (neuronIterator.hasNext()) {
                    Neuron neuron = neuronIterator.next();
                    out.write("    neuron " + neuron.id() + ":");
                    out.newLine();
                    Iterator<Synapse> synapseIterator = neuron.outgoingSynapses().iterator();
                    while (synapseIterator.hasNext()) {
                        Synapse synapse = synapseIterator.next();
                        out.write("             === (" + synapse.weight() + " / " + synapse.lastWeightChange() + " / " + synapse.currentSlope() + " / " + synapse.stepSize() + ")==> neuron " + synapse.destinationNeuron().id());
                        out.newLine();
                        out.write("             slope = " + synapse.currentSlope());
                        out.write("    stepsize = " + synapse.stepSize());
                        out.newLine();
                        out.write("            delta w = " + synapse.lastWeightChange());
                        out.write("    w = " + synapse.weight());
                        out.newLine();
                    }
                }
                out.write(".................................................");
                out.newLine();
            }
            out.write("======================================================");
            out.newLine();
            out.newLine();
            out.close();
        } catch (Exception e) {
            System.out.println("NeuralNetwork: printNetwork: " + e.getMessage());
        }

    }

    public void printErrorToFile(String fileName, TrainingSet traningSet, boolean append) throws Exception {
        try {
            FileWriter fstream = new FileWriter(fileName, append);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("E = " + this.calculateSquaredError(traningSet));
            out.newLine();
            out.close();
        } catch (Exception ex) {
            throw new Exception("NeuralNetwork: printErrorToFile -> " + ex.getMessage());
        }
    }

    public void printErrorToFile(String fileName, TrainingPattern trainingPattern, boolean append) throws Exception {
        try {
            FileWriter fstream = new FileWriter(fileName, append);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("E = " + this.calculateSquaredError(trainingPattern));
            out.newLine();
            out.close();
        } catch (Exception ex) {
            throw new Exception("NeuralNetwork: printErrorToFile -> " + ex.getMessage());
        }
    }

    public void printOutputToFile(String fileName, boolean append) {

    }


}
