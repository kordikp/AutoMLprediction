/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Administrator
 */
public class SlopeCalcFunctionCC implements ISlopeCalcFunction {
    private NeuralNetwork neuralNetwork;
    private transient TrainingSet trainingSet;
    private ArrayList<Synapse> synapsesToTrain;
    private double[][] correlations;
    private DataForCorrelationComp data;
    private NeuronLayer candidateLayer;


    SlopeCalcFunctionCC(NeuronLayer candidateLayer, double[][] correlations, DataForCorrelationComp data) {
        this.candidateLayer = candidateLayer;
        this.correlations = correlations;
        this.data = data;
    }

    private void assimilateVariables(SlopeCalcParams params, TrainingSet trainingSet) throws Exception {
        this.neuralNetwork = params.neuralNetwork;
        this.trainingSet = trainingSet;
        this.synapsesToTrain = params.synapsesToTrain;
    }


    public void calculateSlope(SlopeCalcParams params, TrainingSet trainingSet) throws Exception {
        this.assimilateVariables(params, trainingSet);
        this.neuralNetwork.storeLastSlope();
        this.neuralNetwork.resetSlopes();
        Iterator<TrainingPattern> patternIterator = this.trainingSet.getTraningSet().iterator();
        int patIndex = 0;
        while (patternIterator.hasNext()) {
            TrainingPattern trainingPattern = patternIterator.next();
            this.neuralNetwork.injectInput(trainingPattern.getInputPattern());
            this.neuralNetwork.bubbleThrough();
            Iterator<Neuron> candIterator = this.candidateLayer.neuronList().iterator();
            double change = 0;
            int candIndex = 0;
            while (candIterator.hasNext()) {
                Neuron cand = candIterator.next();
                for (int o = 0; o < this.neuralNetwork.outputLayer().size(); o++) {
                    /*
                    change -= Math.signum(correlations[candIndex][o]) * (data.outputsResidualErrors[o][patIndex]-data.outputsAverageResidualError[o]);
                    change *= cand.calculateDerivative(new ActivationFunctionSigmoid())/data.sumSqError;                    
                     */
                    change -= Math.signum(correlations[candIndex][o]) * (data.outputsResidualErrors[o][patIndex] - data.outputsAverageResidualError[o]);
                    change *= cand.calculateDerivative(new ActivationFunctionSigmoid());
                }
                Iterator<Synapse> synapseIterator = cand.incomingSynapses().iterator();
                while (synapseIterator.hasNext()) {
                    Synapse synapse = synapseIterator.next();
                    synapse.setCurrentSlope(synapse.currentSlope() + change * synapse.transmittedValue());
                }
                candIndex++;
            }
            patIndex++;
        }
    }

    public void calculateSlope(SlopeCalcParams params, TrainingPattern trainingPattern) {

    }
}
