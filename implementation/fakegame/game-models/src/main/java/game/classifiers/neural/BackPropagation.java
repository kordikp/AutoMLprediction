
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

/**
 * /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Implements the backpropagation learning algorithm for Feed-Forward Networks
 */
public class BackPropagation implements ILearningAlgorithm {

    private static final double DEFAULT_LEARNING_RATE = 1.0;
    private static final double DEFAULT_MOMENTUM_RATE = 0.1;
    private double learningRate;
    private double momentumRate;
    private NeuralNetwork neuralNetwork;
    private TrainModel model = TrainModel.batch;


    public BackPropagation(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
        this.learningRate = this.DEFAULT_LEARNING_RATE;
        this.momentumRate = this.DEFAULT_MOMENTUM_RATE;
    }

    public BackPropagation(NeuralNetwork neuralNetwork, double learningRate, double momentumRate) {
        this.neuralNetwork = neuralNetwork;
        this.learningRate = learningRate;
        this.momentumRate = momentumRate;
    }

    public String getType() {
        return "Backpropagation";
    }

    public double learningRate() {
        return this.learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double momentumRate() {
        return this.momentumRate;
    }

    public void setMomentumRate(double momentumRate) {
        this.momentumRate = momentumRate;
    }

    public void modifyWeights(TrainMode mode, ArrayList<Synapse> synapsesToModify) throws Exception {
        Iterator<Synapse> synapseIterator = synapsesToModify.iterator();
        while (synapseIterator.hasNext()) {
            Synapse synapse = synapseIterator.next();
            double weightUpdate = 0;
            if (mode == TrainMode.minimize)
                weightUpdate = -this.learningRate * synapse.currentSlope() + this.momentumRate * synapse.lastWeightChange();
            else
                weightUpdate = this.learningRate * synapse.currentSlope() + this.momentumRate * synapse.lastWeightChange();
            try {
                synapse.addWeight(weightUpdate);
            } catch (Exception e) {
                throw new Exception("BackPropagation: modifyWeightgs -> " + e.getMessage());
            }
        }

    }


    public void train(TrainingSet trainingSet, SlopeCalcParams params, ISlopeCalcFunction slopeCalcFunction) throws Exception {
        if (this.model == TrainModel.batch) {
            slopeCalcFunction.calculateSlope(params, trainingSet);
            modifyWeights(params.mode, params.synapsesToTrain);
        } else {
            Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
            while (patternIterator.hasNext()) {
                slopeCalcFunction.calculateSlope(params, patternIterator.next());
                this.modifyWeights(params.mode, params.synapsesToTrain);
            }
        }
    }

}

