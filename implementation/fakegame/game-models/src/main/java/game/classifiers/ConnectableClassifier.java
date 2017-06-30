package game.classifiers;


import configuration.CfgTemplate;
import configuration.classifiers.ConnectableClassifierConfig;
import game.configuration.Configurable;
import game.data.DataNormalizer;
import game.data.Mode;
import game.data.OutputProducer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import configuration.classifiers.ClassifierConfig;
import game.evolution.treeEvolution.FitnessNode;

/**
 * Wrapper class for models that can be connected to input data and hierarchically
 */
public class ConnectableClassifier implements ClassifierConnectable, OutputProducer, Configurable {
    protected Vector<OutputProducer> inputs;
    protected double output;
    protected DataNormalizer normalizer;
    protected boolean[] selectedInputs;

    public Classifier getClassifier() {
        return classifier;
    }

    protected Classifier classifier;
    protected Mode mode;

    public void init(CfgTemplate config, Vector<OutputProducer> inputs, DataNormalizer normalizer) {
        this.inputs = inputs;
        this.normalizer = normalizer;

        if (config instanceof ConnectableClassifierConfig) {
            ConnectableClassifierConfig cfg = (ConnectableClassifierConfig) config;
            selectedInputs = cfg.getSelectedInputs();
            if (selectedInputs.length == inputs.size()) { //select inputs only if they are same size, otherwise allow all inputs
                Vector<OutputProducer> reducedInputs = new Vector<OutputProducer>();
                for (int i = 0; i < selectedInputs.length; i++) {
                    if (selectedInputs[i]) reducedInputs.add(inputs.get(i));
                }
                this.inputs = reducedInputs;
            } else {
                selectedInputs = new boolean[inputs.size()];
                for (int i = 0; i < selectedInputs.length; i++) selectedInputs[i] = true;
            }

            initClassifier((CfgTemplate) cfg.getNode(0));
        } else {
            initClassifier(config);
        }
    }

    private void initClassifier(CfgTemplate config) {
        Classifier cls;
        ClassifierConfig cfg = (ClassifierConfig) config;
        try {
            cls = (Classifier) config.getClassRef().newInstance();
            cls.init(cfg);
            classifier = cls;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void init(ClassifierConfig cfg) {
        if (classifier != null) classifier.init(cfg);
    }

    public void init(Classifier c, Vector<OutputProducer> inputs) {
        this.inputs = inputs;
        classifier = c;
    }

    public void storeLearningVector(double[] output) {
        if (normalizer == null) {
            classifier.storeLearningVector(getInputVector(), output);
        } else {
            classifier.storeLearningVector(normalizer.normalizeInputVector(getInputVector()), output);
        }
    }

    public Vector<OutputProducer> getInputs() {
        return inputs;
    }

    protected double[] getInputVector() {
        double[] inputVector = new double[inputs.size()];
        int i = 0;
        for (OutputProducer input : inputs) {
            inputVector[i++] = input.getOutput();
        }
        return inputVector;
    }

    /**
     * Computes average input - will be overriden by individual models regarding to their transfer function
     *
     * @return output value
     */
    public double getOutput() {
        if (mode == Mode.PASSIVE) return output;
        if (normalizer == null) output = classifier.getOutput(getInputVector());
        else output = classifier.getOutput(normalizer.normalizeInputVector(getInputVector()));
        return output;
    }

    public String getName() {
        return classifier.getName();
    }

    public void setName(String name) {
        classifier.setName(name);
    }

    public void deleteLearningVectors() {
        classifier.deleteLearningVectors();
    }

    public void resetLearningData() {
        classifier.resetLearningData();
    }

    public void setInputsNumber(int inputs) {
        classifier.setInputsNumber(inputs);
    }

    public void setOutputsNumber(int outputs) {
        classifier.setOutputsNumber(outputs);
    }

    public int getInputsNumber() {
        return classifier.getInputsNumber();
    }

    public int getOutputsNumber() {
        return classifier.getOutputsNumber();
    }

    public Class getConfigClass() {
        return ConnectableClassifierConfig.class;
    }

    public void learn() {
        classifier.learn();
    }

    public void relearn() {
        classifier.relearn();
    }

    public double[] getOutputProbabilities() {
        if (normalizer == null) return classifier.getOutputProbabilities(getInputVector());
        else return classifier.getOutputProbabilities(normalizer.normalizeInputVector(getInputVector()));

    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (normalizer == null) return classifier.getOutputProbabilities(input_vector);
        else return classifier.getOutputProbabilities(normalizer.normalizeInputVector(input_vector));
    }

    public int getOutput(double[] input_vector) {
        if (normalizer == null) return classifier.getOutput(input_vector);

        return classifier.getOutput(normalizer.normalizeInputVector(input_vector));
    }

    public ClassifierConfig getConfig() {
        ConnectableClassifierConfig cfg = null;
        try {
            Constructor constructor = getConfigClass().getConstructor(new Class[]{Integer.TYPE});
            cfg = (ConnectableClassifierConfig) constructor.newInstance(selectedInputs.length);
            cfg.setClassRef(this.getClass());

            boolean[] clonedInputs = new boolean[selectedInputs.length];
            System.arraycopy(selectedInputs, 0, clonedInputs, 0, selectedInputs.length);

            cfg.setSelectedInputs(clonedInputs);
            cfg.addNode((FitnessNode) classifier.getConfig());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public int getMaxLearningVectors() {
        return classifier.getMaxLearningVectors();
    }

    public void setMaxLearningVectors(int maxVectors) {
        classifier.setMaxLearningVectors(maxVectors);
    }

    public void storeLearningVector(double[] input, double[] output) {
        classifier.storeLearningVector(input, output);
    }

    public boolean isLearned() {
        return classifier.isLearned();
    }

    public String toEquation(String[] inputEquation) {
        if (normalizer == null) return classifier.toEquation(inputEquation);

        return classifier.toEquation(normalizer.normalizeInputs(inputEquation));
    }

    public String[] getEquations(String[] inputEquation) {
        if (normalizer == null) return classifier.getEquations(inputEquation);

        return classifier.getEquations(normalizer.normalizeInputs(inputEquation));
    }

    public String toEquation() {
        String[] inputEquation = new String[inputs.size()];
        int i = 0;
        for (OutputProducer input : inputs) {
            inputEquation[i++] = input.toEquation();
        }
        return classifier.toEquation(inputEquation);
    }

    public void connectTo(Vector<OutputProducer> inputs) {
        this.inputs = inputs;
    }

    public void setInput(int index, OutputProducer input) {
        inputs.set(index, input);
    }

    public void disconnect() {
        inputs = null;
    }

    public void disconnectInput(int index) {
        inputs.removeElementAt(index);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void toPassiveMode() {
        getOutput();
        setMode(Mode.PASSIVE);
    }

    public double[][] getLearningInputVectors() {
        return classifier.getLearningInputVectors();
    }

    public double[][] getLearningOutputVectors() {
        return classifier.getLearningOutputVectors();
    }
}
