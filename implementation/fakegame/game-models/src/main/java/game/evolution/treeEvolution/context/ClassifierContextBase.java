package game.evolution.treeEvolution.context;

import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ensemble.EnsembleClassifierConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import game.classifiers.ConnectableClassifier;
import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.InnerFitnessNode;


public abstract class ClassifierContextBase extends FitnessContextBase {

    protected ConnectableClassifier bestModel;

    protected Fitness getModelFitness(FitnessNode obj) {
        return getModelFitnessLearnedOnData(obj, learnIndex, validIndex);
    }

    protected Fitness getFitnessOnLearnValid(FitnessNode node) {
        return getModelFitnessLearnedOnData(node, finalLearnIndex, validIndex);
    }

    protected Fitness getModelFitnessLearnedOnData(FitnessNode node, int[] learnIndexes, int[] validIndexes) {
        ConnectableClassifier cls = initClassifier((ClassifierConfig) node, learnIndexes);
        cls.learn();

        return evaluateClassifier(cls, node, validIndexes, testIndex);
    }

    protected ConnectableClassifier initClassifier(ClassifierConfig cfg, int[] learnIndexes) {
        ConnectableClassifier cls = new ConnectableClassifier();
        cls.init(cfg, inp, norm);
        cls.setMaxLearningVectors(learnIndexes.length);

        for (int i = 0; i < learnIndexes.length; i++) {
            data.publishVector(learnIndexes[i]);
            cls.storeLearningVector(data.getTargetOutputs());
        }
        return cls;
    }

    protected double performTestOnData(ConnectableClassifier cls, int[] dataField) {
        int chosenClass;
        double[] outputClass;
        int right = 0;

        for (int i = 0; i < dataField.length; i++) {
            data.publishVector(dataField[i]);
            chosenClass = (int) cls.getOutput();
            outputClass = data.getTargetOutputs();
            if (outputClass[chosenClass] == 1) right++;
        }
        return (double) right / dataField.length;
    }

    protected int getNumberOfModels(InnerFitnessNode node) {
        int sum = 0;
        for (int i = 0; i < node.getNodesNumber(); i++) {
            if (node.getNode(i) instanceof InnerFitnessNode) {
                sum += getNumberOfModels((InnerFitnessNode) node.getNode(i));
            } else {
                sum++;
            }
        }
        int coef = 1;
        if (node instanceof EnsembleClassifierConfig) coef = ((EnsembleClassifierConfig) node).getClassifiersNumber();
        else if (node instanceof ClassifierModelConfig) coef = data.getONumber();

        return sum / node.getNodesNumber() * coef;
    }

    protected Fitness evaluateClassifier(ConnectableClassifier cls, FitnessNode cfg, int[] validIndex, int[] testIndex) {
        double validFitness = performTestOnData(cls, validIndex);
        double testFitness;
        if (testIndex.length == 0) testFitness = validFitness;
        else testFitness = performTestOnData(cls, testIndex);
        //CRITICAL SECTION
        if (parallelLock != null) getLock();
        if (testFitness > bestFitness) {
            bestFitness = testFitness;
            bestModel = cls;
            bestModelConfig = cfg;
        }
        if (parallelLock != null) parallelLock.release();
        return new Fitness(validFitness, testFitness);
    }

    /**
     * Set and get functions
     */

    public ConnectableClassifier getBestModel() {
        return bestModel;
    }

    public FitnessNode getBestModelPredefinedConfig() {
        return (FitnessNode) bestModel.getConfig();
    }

}
