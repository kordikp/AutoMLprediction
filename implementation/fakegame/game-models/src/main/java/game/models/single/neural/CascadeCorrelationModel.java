package game.models.single.neural;

import configuration.models.ModelConfig;
import configuration.models.single.neural.CascadeCorrelationModelConfig;
import game.classifiers.neural.*;

/**
 *
 */
public class CascadeCorrelationModel extends NeuralModel {

    protected CascadeCorrelation cc;

    protected int maxLayersNumber;
    protected int candNumber;
    protected String usedAlgName;
    protected int usedAlg;

    @Override
    public Class getConfigClass() {
        return CascadeCorrelationModelConfig.class;
    }

    @Override
    public void init(ModelConfig cfg) {
        CascadeCorrelationModelConfig cf = (CascadeCorrelationModelConfig) cfg;
        acceptableError = cf.getAcceptableError();
        maxLayersNumber = cf.getMaxLayersNumber();
        candNumber = cf.getCandNumber();
        usedAlgName = cf.getUsedAlg().getEnabledElements(String.class)[0];
        activationFunctionName = cf.getActivationFunction().getEnabledElements(String.class)[0];
        super.init(cfg);
    }

    @Override
    public void learn() {
        trainingSet = new TrainingSet();
        for (int i = 0; i < learning_vectors; i++) {
            double[] criteria = new double[1];
            criteria[0] = target[i];
            TrainingPattern pattern = new TrainingPattern(inputVect[i], criteria);
            trainingSet.addTrainingPattern(pattern);
        }
        initNetwork();
        try {
            cc.trainNetwork(trainingSet);
        } catch (Exception ex) {

        }
        learned = true;
    }

    @Override
    public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputsNumber; i++) {
            s += inputEquation[i] + ((i < inputsNumber - 1) ? ", " : "");
        }
        return "Cascade_correlation(" + s + ")";
    }

    private void setAlgorithm() {
        if (usedAlgName.equals("Backpropagation")) usedAlg = 1;
        else if (usedAlgName.equals("Quickprop")) usedAlg = 2;
        else if (usedAlgName.equals("Rprop")) usedAlg = 3;
    }

    private void initNetwork() {
        try {
            super.setActivationFunction();
            network = new NeuralNetwork(inputsNumber, 1, 1, activationFunction, true);

            this.setAlgorithm();
            cc = new CascadeCorrelation(network, maxLayersNumber, acceptableError, candNumber, true, usedAlg);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

