package game.classifiers.single;

import game.classifiers.ClassifierBase;
import game.classifiers.ensemble.ClassifierWeighted;
import game.classifiers.neural.ActivationFunctionSigmoidFahlmanOffset;
import game.classifiers.neural.CascadeCorrelation;
import game.classifiers.neural.NeuralNetwork;
import game.classifiers.neural.TrainingPattern;
import game.classifiers.neural.TrainingSet;
import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.single.NeuralClassifierConfig;

/**
 * This class provide access to neural network library - it can build back propagation, cascade correlation, etc. models
 */
public class NeuralClassifier extends ClassifierBase {


    @Override
    public Class getConfigClass() {
        return NeuralClassifierConfig.class;
    }

    private NeuralNetwork network;
    private transient TrainingSet trainingSet;
    private CascadeCorrelation cc;
    private double acceptableError;
    private int maxNumberLayer;
    private int candMaxUpdateCycles;
    private double minCorrGrowth;


    private double candEpsilon;
    private double candMaxAlfa;
    private double candDecay;

    private int outMaxUpdateCycles;
    private double minErrRed;
    private double outEpsilon;
    private double outMaxAlfa;
    private double outDecay;

    public void init(ClassifierConfig cfg) {
        NeuralClassifierConfig cf = (NeuralClassifierConfig) cfg;
        acceptableError = cf.getAcceptableError();
        maxNumberLayer = cf.getMaxNumberLayer();
        candMaxUpdateCycles = cf.getCandMaxUpdateCycles();
        minCorrGrowth = cf.getMinCorrGrowth();
        candEpsilon = cf.getCandEpsilon();
        candMaxAlfa = cf.getCandMaxAlfa();
        candDecay = cf.getCandDecay();
        outMaxUpdateCycles = cf.getOutMaxUpdateCycles();
        minErrRed = cf.getMinErrRed();
        outEpsilon = cf.getOutEpsilon();
        outMaxAlfa = cf.getOutMaxAlfa();
        outDecay = cf.getOutDecay();
        super.init(cfg);
        initCascade();
    }

    public void learn() {
        trainingSet = new TrainingSet();
        for (int i = 0; i < learning_vectors; i++) {
            TrainingPattern pattern = new TrainingPattern(inputVect[i], target[i]);
            trainingSet.addTrainingPattern(pattern);
        }
        try {
            this.cc.trainNetwork(trainingSet);
        } catch (Exception ex) {

        }
        learned = true;
    }

    public void relearn() {
        learn();
    }

    public void learn(int modelIndex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        TrainingPattern pattern = new TrainingPattern(input_vector, new double[1]);
        double[] output = new double[outputs];
        try {
            output = this.network.extractOutput(pattern);
            return output;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output;
    }

    /**
     * Output of the classifier
     *
     * @param input_vector Specify inputs to the classifer
     * @return index of the winning class
     */
    public int getOutput(double[] input_vector) {
        TrainingPattern pattern = new TrainingPattern(input_vector, new double[1]);
        double[] output = new double[outputs];
        try {
            output = this.network.extractOutput(pattern);
            return ClassifierWeighted.maxIndex(output);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public double getOutputTo(int inputVectorIndex) {
        return getOutput(inputVect[inputVectorIndex]);
    }

    public String toEquation(String[] inputEquation) {
        String s = "";
        for (int i = 0; i < inputs; i++) {
            s += inputEquation[i] + ((i < inputs - 1) ? ", " : "");
        }
        return "Cascade_correlation(" + s + ")";
    }

    public String[] getEquations(String[] inputEquation) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int[] getTargetVariables() {
        return new int[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteLearningVectors() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void initCascade() {

        try {
            network = new NeuralNetwork(inputs, outputs, 1, new ActivationFunctionSigmoidFahlmanOffset(), true);
            //cc = new CascadeCorrelation(network,this.outMaxUpdateCycles,this.candMaxUpdateCycles,this.minErrRed,this.acceptableError,
            //this.maxNumberLayer, 10, 10, this.minCorrGrowth, this.outDecay, this.candDecay);
            cc = new CascadeCorrelation(network, 4, acceptableError, 10, true, CascadeCorrelation.QUICK_PROPAGATION_ALG);
            cc.setQuickParams();
            //todo pass parameters
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}