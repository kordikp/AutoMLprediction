package game.models;

import configuration.CfgTemplate;
import configuration.models.ConnectableModelConfig;
import game.data.DataNormalizer;
import game.data.Mode;
import game.data.OutputProducer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import configuration.models.ModelConfig;
import game.evolution.treeEvolution.FitnessNode;

/**
 * Wrapper class for models that can be connected to input data and hierarchically
 */
public class ConnectableModel implements ModelConnectable, OutputProducer {
    protected Vector<OutputProducer> inputs;
    transient protected double output;
    protected DataNormalizer normalizer;
    protected ModelLearnable model;
    protected Mode mode;
    protected boolean[] selectedInputs;

    public void init(CfgTemplate modelConfig, Vector<OutputProducer> inputs, DataNormalizer normalizer) {
        this.inputs = inputs;
        this.normalizer = normalizer;

        if (modelConfig instanceof ConnectableModelConfig) {
            ConnectableModelConfig cfg = (ConnectableModelConfig) modelConfig;
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

            initModel((CfgTemplate) cfg.getNode(0));
        } else {
            initModel(modelConfig);
        }
    }

    private void initModel(CfgTemplate config) {
        ModelLearnable model;
        ModelConfig cfg = (ModelConfig) config;
        try {
            model = (ModelLearnable) config.getClassRef().newInstance();
            model.init(cfg);
            this.model = model;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void storeLearningVector(double output) {
        if (normalizer == null) {
            model.storeLearningVector(getInputVector(), output);
        } else
            model.storeLearningVector(normalizer.normalizeInputVector(getInputVector()), normalizer.normalizeTarget(output));
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
        if (normalizer == null) {
            if (mode == Mode.PASSIVE) return output;
            output = model.getOutput(getInputVector());
        } else {
            if (mode == Mode.PASSIVE) return output;
            output = normalizer.denormalizeTarget(model.getOutput(normalizer.normalizeInputVector(getInputVector())));
        }
        return output;
    }

    public ModelConfig getConfig() {
        ConnectableModelConfig cfg = null;
        try {
            Constructor constructor = getConfigClass().getConstructor(new Class[]{Integer.TYPE});
            cfg = (ConnectableModelConfig) constructor.newInstance(selectedInputs.length);
            cfg.setClassRef(this.getClass());

            boolean[] clonedInputs = new boolean[selectedInputs.length];
            System.arraycopy(selectedInputs, 0, clonedInputs, 0, selectedInputs.length);

            cfg.setSelectedInputs(clonedInputs);
            cfg.addNode((FitnessNode) model.getConfig());
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

    public String getName() {
        return model.getName();
    }

    public void setName(String name) {
        model.setName(name);
    }

    public String getTrainedBy() {
        return model.getTrainedBy();
    }

    public void setTrainedBy(String trainerName) {
        model.setTrainedBy(trainerName);
    }

    public double getOutput(double[] input_vector) {
        if (normalizer == null) return model.getOutput(input_vector);
        return normalizer.denormalizeTarget(model.getOutput(normalizer.normalizeInputVector(input_vector)));
    }

    public int getTargetVariable() {
        return model.getTargetVariable();
    }

    public void setTargetVariable(int targetVariable) {
        model.setTargetVariable(targetVariable);
    }

    public String toEquation(String[] inputEquation) {
        if (normalizer == null) return model.toEquation(inputEquation);
        return model.toEquation(normalizer.normalizeInputs(inputEquation));
    }

    public Class getConfigClass() {
        return ConnectableModelConfig.class;
    }

    public String toEquation() {
        String[] inputEquation = new String[inputs.size()];
        int i = 0;
        for (OutputProducer input : inputs) {
            inputEquation[i++] = input.toEquation();
        }
        return toEquation(inputEquation);
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

    public Model getModel() {
        return model;
    }

    @Deprecated
    public double getNormalizedOutput(double[] normalized_input_vector) {
        return model.getNormalizedOutput(normalized_input_vector);
    }

}