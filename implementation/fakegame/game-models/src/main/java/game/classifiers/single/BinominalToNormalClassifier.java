package game.classifiers.single;

import configuration.classifiers.ClassifierConfig;
import game.classifiers.Classifier;
import game.classifiers.ClassifierBase;
import game.evolution.treeEvolution.context.InterruptibleArrayList;

import java.util.ArrayList;

/**
 * Class that takes configuration of classifiers that are binominal and performs decomposition of data to N binominal problems.
 */
public abstract class BinominalToNormalClassifier extends ClassifierBase {

    protected ArrayList<Classifier> classifiers;
    protected ClassifierConfig classifierCfg;
    protected int[] outputMap;

    protected abstract Classifier getClassifierInstance();

    public void init(ClassifierConfig cfg) {
        super.init(cfg);
        classifierCfg = cfg;
        classifiers = new InterruptibleArrayList<Classifier>();
    }

    /**
     * Computes classifier map and initializes all classifiers. If column has all zeroes, do not create model for that
     * output.
     *
     * @param outputs Number of output attributes.
     */
    protected void initClassifier(int outputs) {
        binarizeData(target);

        int[] classifierMap = new int[outputs];
        outputMap = new int[outputs];
        int modelIndex = 0;
        for (int i = 0; i < target[0].length; i++) {
            if (hasColumnSameValues(target, i) && target[0][i] == 0) {
                //-1 = output all zeroes
                outputMap[i] = -1;
            } else {
                classifierMap[modelIndex] = i;
                outputMap[i] = modelIndex;
                modelIndex++;
            }
        }

        if (modelIndex == 1) {
            for (int i = 0; i < outputMap.length; i++) {
                if (outputMap[i] == 0) {
                    //-2 set model output to all 1
                    outputMap[i] = -2;
                    return;
                }
            }
        }

        int numClassifiers;
        if (outputs == 2 && modelIndex == 2) numClassifiers = 1;
        else numClassifiers = modelIndex;

        double[] binominalOutput = new double[2];
        int classifierColumn;
        Classifier cls;
        for (int i = 0; i < numClassifiers; i++) {
            cls = getClassifierInstance();
            cls.init(classifierCfg);
            cls.setMaxLearningVectors(maxLearningVectors);
            classifierColumn = classifierMap[i];
            for (int j = 0; j < learning_vectors; j++) {
                binominalOutput[0] = target[j][classifierColumn];
                binominalOutput[1] = 1 - binominalOutput[0];
                cls.storeLearningVector(inputVect[j], binominalOutput);
            }
            classifiers.add(cls);
        }
    }

    public void learn() {
        if (classifierCfg != null) {
            initClassifier(target[0].length);
            classifierCfg = null;
        }

        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).learn();
        }
        learned = true;
    }

    public void relearn() {
        learn();
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (classifiers.size() == 1) return classifiers.get(0).getOutputProbabilities(input_vector);

        double[] output = new double[outputs];
        for (int i = 0; i < output.length; i++) {
            if (outputMap[i] == -1) output[i] = 0;
            else if (outputMap[i] == -2) output[i] = 1;
            else output[i] = classifiers.get(outputMap[i]).getOutputProbabilities(input_vector)[0];
        }
        return output;
    }

    @Override
    public void setMaxLearningVectors(int maxVectors) {
        super.setMaxLearningVectors(maxVectors);
        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).setMaxLearningVectors(maxVectors);
        }
    }

    @Override
    public boolean isLearned() {
        if (classifiers.size() == 0) return false;
        else return classifiers.get(0).isLearned();
    }

    @Override
    public String toEquation(String[] inputEquation) {
        return null;
    }

    public String[] getEquations(String[] inputEquation) {
        return new String[0];
    }

    @Override
    public void deleteLearningVectors() {
        super.deleteLearningVectors();
        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).deleteLearningVectors();
        }
    }

    @Override
    public void resetLearningData() {
        super.resetLearningData();
        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).resetLearningData();
        }
    }

    @Override
    public void setInputsNumber(int inputs) {
        super.setInputsNumber(inputs);
        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).setInputsNumber(inputs);
        }
    }

    @Override
    public void setOutputsNumber(int outs) {
        super.setOutputsNumber(outs);
        for (int i = 0; i < classifiers.size(); i++) {
            classifiers.get(i).setOutputsNumber(outs);
        }
    }

    private boolean hasColumnSameValues(double[][] data, int columnIndex) {
        double value = data[0][columnIndex];
        for (int i = 1; i < data.length; i++) {
            if (data[i][columnIndex] != value) return false;
        }
        return true;
    }

    private void binarizeData(double[][] data) {
        int maxIndex;
        for (int i = 0; i < data.length; i++) {
            maxIndex = maxIndex(data[i]);
            for (int j = 0; j < data[0].length; j++) data[i][j] = 0;
            data[i][maxIndex] = 1;
        }
    }

    private int maxIndex(double[] array) {
        double max = array[0];
        int maxIdx = 0;

        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}
