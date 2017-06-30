
package game.classifiers.neural;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents neuron layer
 *
 * @author Do Minh Duc
 */
public class NeuronLayer implements Serializable {
    private ArrayList<Neuron> neuronsList;
    private LayerType layerType;
    private static final int BIAS_VALUE = 1;
    private int biasNumber;

    /**
     * Constructor
     *
     * @param numberOfNeurons    number of neurons
     * @param layerType          type of layer
     * @param neuronId           neuron id, for debugging
     * @param biasNumber         number of bias neurons
     * @param activationFunction activation function
     */
    public NeuronLayer(int numberOfNeurons, LayerType layerType, int neuronId, int biasNumber, IActivationFunction activationFunction) throws Exception {
        //if (layerType != LayerType.input && biasNumber > 0) throw new Exception ("NeuronLayer: constructor: not input layer is not allowed to have bias Neuron");
        this.neuronsList = new ArrayList<Neuron>();
        this.layerType = layerType;
        NeuronType neuronType = HelpingFunctions.determineNeuronType(layerType);
        for (int i = 0; i < numberOfNeurons; i++) {
            this.addNeuron(neuronType, activationFunction, neuronId);
            neuronId++;
        }
        for (int i = 0; i < biasNumber; i++) {
            this.addNeuron(NeuronType.bias, activationFunction, neuronId);
            neuronId++;
        }
        this.biasNumber = biasNumber;
    }

    /**
     * Returns number of bias neurons
     *
     * @return number of bias neurons
     */
    public int biasNumber() {
        return this.biasNumber;
    }

    /**
     * Returns arraylist with all neurons
     *
     * @return arraylist with al neurons
     */
    public ArrayList<Neuron> neuronList() {
        return this.neuronsList;
    }

    /**
     * Returns type of layer
     *
     * @return type of layer
     */
    public LayerType type() {
        return this.layerType;
    }

    /**
     * Returns number of neurons
     *
     * @return number of neurons
     */
    public int size() {
        return this.neuronsList.size();
    }

    /**
     * Returns neuron at specified position
     *
     * @param index neuron's position
     * @return
     */
    public Neuron getNeuron(int index) {
        if (index < 0 || index >= this.size()) return null;
        else return this.neuronsList.get(index);
    }

    /**
     * Adds neuron
     *
     * @param neuronType         type of neuron
     * @param activationFunction activation function
     * @param neuronId           neuron id
     */
    public void addNeuron(NeuronType neuronType, IActivationFunction activationFunction, int neuronId) {
        Neuron newNeuron = new Neuron(neuronType, activationFunction, this, neuronId);
        if (neuronType == NeuronType.bias) newNeuron.setOutput(this.BIAS_VALUE);
        this.neuronsList.add(newNeuron);
    }


    //**************PREZKOUMAT JESTLI PONECHAT
    /*
    public void addNeuron(NeuronType neuronType, int neuronId){
        Neuron newNeuron = new Neuron (neuronType, this, neuronId);
        if (neuronType == NeuronType.bias) newNeuron.setOutputValue(this.BIAS_VALUE);
        this.neuronsList.add(newNeuron);
    }

     * */

    /**
     * Removes neuron
     *
     * @param neuron neuron to be removed
     */
    public void removeNeuron(Neuron neuron) {
        Iterator<Synapse> iterator = neuron.incomingSynapses().iterator();
        while (iterator.hasNext()) {
            Synapse synapse = iterator.next();
            synapse.sourceNeuron().removeOutgoingSynapse(synapse);
        }
        iterator = neuron.outgoingSynapses().iterator();
        while (iterator.hasNext()) {
            Synapse synapse = iterator.next();
            synapse.destinationNeuron().removeIncomingSynapse(synapse);
        }
        neuron.incomingSynapses().clear();
        neuron.outgoingSynapses().clear();
        this.neuronList().remove(neuron);
    }


    /**
     * Checks if all neurons are the same type as the layer
     *
     * @param neuronList
     * @param layerType
     * @return true if there is no error
     */
    private boolean checkLayerTypeConsistency(LayerType layerType) {
        NeuronType neuronType = HelpingFunctions.determineNeuronType(layerType);
        Iterator<Neuron> iterator = this.neuronsList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().type() != neuronType) return false;
        }
        return true;
    }


}
