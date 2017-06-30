/*
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Administrator
 */
public class QuickPropagation implements ILearningAlgorithm {
    private NeuralNetwork neuralNetwork;
    private double momentumRate;
    private double decay;
    private double epsilon;
    private double maxAlfa;
    private double initialEpsilon;
    private TrainModel model;
    private SlopeCalcParams standardPartialDerivativeInfo;
    private boolean splitEpsilon;

    private static final double DEFAULT_DECAY = 0.0001;
    private static final double DEFAULT_EPSILON = 0.0007;
    private static final double DEFAULT_MAX_ALFA = 2;
    private static final boolean DEFAULT_SPLIT_EPSILON = false;


    @SuppressWarnings("static-access")
    public QuickPropagation(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
        this.maxAlfa = this.DEFAULT_MAX_ALFA;
        this.decay = this.DEFAULT_DECAY;
        this.initialEpsilon = this.DEFAULT_EPSILON;
        this.epsilon = this.initialEpsilon;
        this.model = TrainModel.batch;
        this.splitEpsilon = this.DEFAULT_SPLIT_EPSILON;
    }

    public QuickPropagation(NeuralNetwork neuralNetwork, double maxAlfa, double decay, double initialEpsilon, boolean splitEpsilon) {
        this.neuralNetwork = neuralNetwork;
        this.maxAlfa = maxAlfa;
        this.decay = decay;
        this.initialEpsilon = initialEpsilon;
        this.epsilon = initialEpsilon;
        this.model = TrainModel.batch;
        this.splitEpsilon = splitEpsilon;
    }

    public String getType() {
        return "Quickpropagation";
    }

    public void setModel(TrainModel newModel) {
        this.model = newModel;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setDecay(double decay) {
        this.decay = decay;
    }

    public void setSplitEpsilon(boolean splitEpsilon) {
        this.splitEpsilon = splitEpsilon;
    }

    public double calculateEpsilon(Synapse synapse) {
        if (synapse.previousSlope() == 0 || synapse.currentSlope() * synapse.previousSlope() > 0) {
            if (this.splitEpsilon && synapse.destinationNeuron().incomingSynapses().size() != 0)
                return this.epsilon / synapse.destinationNeuron().incomingSynapses().size();
            else return this.epsilon;
        } else return 0;
    }

    public double calculateAlfa(Synapse synapse) {
        double alfaBar = calculateAlfaBar(synapse);
        if (isAlfaInfinite(alfaBar) || isAlfaTooLarge(alfaBar, synapse) || isAlfaUphill(alfaBar, synapse))
            return this.maxAlfa;
        else return alfaBar;

    }

    public double calculateAlfaBar(Synapse synapse) {
        return synapse.currentSlope() / (synapse.previousSlope() - synapse.currentSlope());
    }

    public boolean isAlfaInfinite(double alfa) {

        return (Double.isInfinite(alfa) || Double.isNaN(alfa));
    }

    public boolean isAlfaTooLarge(double alfa, Synapse synapse) {
        return (alfa > this.maxAlfa);
    }


    public boolean isAlfaUphill(double alfa, Synapse synapse) {
        return (alfa * synapse.lastWeightChange() * synapse.currentSlope() > 0);
    }


    public void train(TrainingSet trainingSet, SlopeCalcParams params, ISlopeCalcFunction slopeCalcFunction) throws Exception {
        if (this.model == TrainModel.batch) {
            slopeCalcFunction.calculateSlope(params, trainingSet);
            modifyWeights(params.mode, params.synapsesToTrain);
        } else {
            Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
            while (patternIterator.hasNext()) {
                TrainingPattern trainingPattern = patternIterator.next();
                slopeCalcFunction.calculateSlope(params, trainingPattern);
                modifyWeights(params.mode, params.synapsesToTrain);
            }
        }

    }

    public void modifyWeights(TrainMode mode, ArrayList<Synapse> synapsesToModify) throws Exception {

        for (int i = 0; i < synapsesToModify.size(); i++) {
            Synapse synapse = synapsesToModify.get(i);
            double alfa = calculateAlfa(synapse);
            double usedEpsilon = calculateEpsilon(synapse);
            double weightChange;
            if (Double.isInfinite(synapse.weight()) || Double.isNaN(synapse.weight())) {
                System.out.println();
            }
            if (mode == TrainMode.maximize) this.invertPartialDerivative(synapsesToModify);
            weightChange = -usedEpsilon * synapse.currentSlope() + alfa * synapse.lastWeightChange();
            try {
                synapse.addWeight(weightChange);
            } catch (Exception ex) {
                throw new Exception("QuickPropagation: modifyWeights -> " + ex.getMessage());
            }
        }
        
         
        /*
        Iterator<Synapse> synapseIterator = synapsesToModify.iterator();
        while (synapseIterator.hasNext()){
            Synapse synapse = synapseIterator.next();
            double change;
            if (synapse.previousSlope() == 0){
                change = -epsilon*synapse.currentSlope();
            }
            else{
                if (synapse.currentSlope()*(Math.signum(synapse.previousSlope())) >= (this.maxAlfa / (this.maxAlfa+1))*Math.abs(synapse.previousSlope())){
                    change = this.maxAlfa*synapse.lastWeightChange();
                }
                else{
                    change = synapse.currentSlope()*synapse.lastWeightChange() / (synapse.previousSlope() - synapse.currentSlope());
                }
                if (Math.signum(synapse.previousSlope()) == Math.signum(synapse.currentSlope())){
                    change -= this.epsilon*synapse.currentSlope();
                }
            }
            synapse.addWeight(change);
        }
        
        */
        /*
        Iterator<Synapse> synapseIterator = synapsesToModify.iterator();
        while (synapseIterator.hasNext()){
            Synapse synapse = synapseIterator.next();
            double weightChange = 0;
            double shrinkFactor = this.maxAlfa / (1-this.maxAlfa);
            if (synapse.lastWeightChange() < 0){
                if (synapse.currentSlope()>0)
                    weightChange -= this.epsilon*synapse.currentSlope();
                if (synapse.currentSlope() >= shrinkFactor*synapse.previousSlope())
                    weightChange += this.maxAlfa*synapse.lastWeightChange();
                else weightChange += synapse.lastWeightChange()*synapse.currentSlope()/(synapse.lastWeightChange() - synapse.previousSlope());
                
            }
            else{
                if (synapse.lastWeightChange() > 0){
                    if (synapse.currentSlope() < 0)
                        weightChange -= this.epsilon*synapse.currentSlope();
                    if (synapse.currentSlope() <= shrinkFactor*synapse.previousSlope())
                        weightChange += synapse.lastWeightChange()*this.maxAlfa;
                    else weightChange += synapse.lastWeightChange()*synapse.currentSlope()/(synapse.previousSlope() - synapse.currentSlope());
         
                }
                else weightChange -= this.epsilon*synapse.currentSlope();
                        
            }
            synapse.addWeight(weightChange);
        }
        
          */

    }

    private void invertPartialDerivative(ArrayList<Synapse> synapsesToInvert) {
        Iterator<Synapse> synapseIterator = synapsesToInvert.iterator();
        while (synapseIterator.hasNext()) {
            Synapse synapse = synapseIterator.next();
            synapse.setCurrentSlope(-synapse.currentSlope());
            synapse.setPreviousSlope(-synapse.previousSlope());
        }
    }

}
