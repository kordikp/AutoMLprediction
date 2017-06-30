
package game.classifiers.neural;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Administrator
 */
public class RProp implements ILearningAlgorithm {
    private static final double DEFAULT_INITIAL_STEP_SIZE = 0.1;
    private double initialStepSize;
    private double etaMinus = 0.5;
    private double etaPlus = 1.2;
    private double stepSizeMin = 0.000001;
    private double stepSizeMax = 50;
    private NeuralNetwork network;
    private TrainModel model;


    public RProp(NeuralNetwork network) {
        this.network = network;
        this.model = TrainModel.batch;
        this.setInitialStepSize(network.synapses());

    }

    public RProp(NeuralNetwork network, double etaMinus, double etaPlus) {
        this.etaMinus = etaMinus;
        this.etaPlus = etaPlus;
        this.model = TrainModel.batch;
        this.setInitialStepSize(network.synapses());
    }

    public RProp(ArrayList<Synapse> synapsesToTrain) {
        this.setInitialStepSize(synapsesToTrain);
        this.model = TrainModel.batch;
    }

    public String getType() {
        return "RProp";
    }

    public void setInitialStepSize(ArrayList<Synapse> synapsesToTrain) {
        Iterator<Synapse> synapseIterator = synapsesToTrain.iterator();
        while (synapseIterator.hasNext()) {
            synapseIterator.next().setStepSize(this.DEFAULT_INITIAL_STEP_SIZE);
        }
    }

    public void setModel(TrainModel model) {
        this.model = model;
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
        Iterator<Synapse> synapseIterator = synapsesToModify.iterator();
        while (synapseIterator.hasNext()) {
            Synapse synapse = synapseIterator.next();
            if (synapse.currentSlope() * synapse.previousSlope() > 0) {
                double stepSize = synapse.stepSize() * this.etaPlus;
                if (stepSize > this.stepSizeMax) stepSize = this.stepSizeMax;
                synapse.setStepSize(stepSize);
                double weightChange = -Math.signum(synapse.currentSlope()) * stepSize;
                try {
                    synapse.addWeight(weightChange);
                } catch (Exception ex) {
                    throw new Exception("RProp: modifyWeights -> " + ex.getMessage());
                }
                //synapse.storePartialDerivativeIntoLastPartialDerivative();
            } else {
                if (synapse.currentSlope() * synapse.previousSlope() < 0) {
                    double stepSize = synapse.stepSize() * this.etaMinus;
                    if (stepSize < this.stepSizeMin) stepSize = this.stepSizeMin;
                    synapse.setStepSize(stepSize);
                    synapse.setCurrentSlope(0);
                    //synapse.storePartialDerivativeIntoLastPartialDerivative();
                } else {
                    double weightChange = -Math.signum(synapse.currentSlope()) * synapse.stepSize();
                    try {
                        synapse.addWeight(weightChange);
                    } catch (Exception ex) {
                        throw new Exception("RProp: modifyWeights -> " + ex.getMessage());
                    }
                    //synapse.storePartialDerivativeIntoLastPartialDerivative();
                }
            }
        }

    }

}
