/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.util.Iterator;

/**
 * @author Administrator
 */
public class SlopeCalcFunctionQuickProp implements ISlopeCalcFunction {
    public SlopeCalcFunctionQuickProp() {

    }

    private void calculateSlope(Synapse synapse, SlopeCalcParams params, boolean accumulate) {
        double slope = synapse.destinationNeuron().currentDelta() * synapse.sourceNeuron().currentOutput();
        //slope += params.decay * synapse.weight();
        if (accumulate) synapse.setCurrentSlope(slope + synapse.currentSlope());
        else synapse.setCurrentSlope(slope);
    }

    private void calculateDeltaForOutputs(SlopeCalcParams params, Pattern desiredOutputs) throws Exception {
        NeuralNetwork neuralNetwork = params.neuralNetwork;
        Iterator<Neuron> neuronIterator = neuralNetwork.outputLayer().neuronList().iterator();
        int index = 0;
        while (neuronIterator.hasNext()) {
            neuronIterator.next().calculateDelta(desiredOutputs.get(index++));
        }
    }

    private void calculateDeltaForHiddens(SlopeCalcParams params) {
        NeuralNetwork neuralNetwork = params.neuralNetwork;
        Iterator<NeuronLayer> layerIterator = neuralNetwork.hiddenLayers().iterator();
        while (layerIterator.hasNext()) {
            Iterator<Neuron> neuronIterator = layerIterator.next().neuronList().iterator();
            while (neuronIterator.hasNext()) {
                neuronIterator.next().calculateDelta();
            }
        }
    }

    private void calculateDelta(SlopeCalcParams params, TrainingPattern trainingPattern) throws Exception {
        try {
            params.neuralNetwork.injectInput(trainingPattern.getInputPattern());
            params.neuralNetwork.bubbleThrough();
            this.calculateDeltaForOutputs(params, trainingPattern.getDesiredOutputs());
            this.calculateDeltaForHiddens(params);
        } catch (Exception e) {
            throw new Exception("SlopeCalcFunctionQuickProp: calculateDelta -> " + e.getMessage());
        }

    }

    public void calculateSlope(SlopeCalcParams slopeParams, TrainingSet trainingSet) throws Exception {
        slopeParams.neuralNetwork.storeLastSlope(slopeParams.synapsesToTrain);
        slopeParams.neuralNetwork.resetSlopes();
        Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
        while (patternIterator.hasNext()) {
            TrainingPattern trainingPattern = patternIterator.next();
            this.calculateDelta(slopeParams, trainingPattern);
            boolean accumulate = true;
            Iterator<Synapse> synapseIterator = slopeParams.synapsesToTrain.iterator();
            while (synapseIterator.hasNext()) {
                calculateSlope(synapseIterator.next(), slopeParams, accumulate);
            }
        }
    }

    public void calculateSlope(SlopeCalcParams params, TrainingPattern trainingPattern) throws Exception {
        params.neuralNetwork.storeLastSlope(params.synapsesToTrain);
        params.neuralNetwork.resetDeltas();
        params.neuralNetwork.resetSlopes();
        params.neuralNetwork.injectInput(trainingPattern.getInputPattern());
        params.neuralNetwork.bubbleThrough();
        try {
            this.calculateDelta(params, trainingPattern);
        } catch (Exception ex) {
            throw new Exception("SlopeCalcFunctionQuickProp: calculateSlope -> " + ex.getMessage());
        }
        boolean accumulate = false;
        Iterator<Synapse> synapseIterator = params.synapsesToTrain.iterator();
        while (synapseIterator.hasNext()) {
            this.calculateSlope(synapseIterator.next(), params, accumulate);
        }
    }


}
