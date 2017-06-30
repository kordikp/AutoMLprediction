package game.evolution.treeEvolution.context;

import configuration.models.ModelConfig;
import configuration.models.ensemble.EnsembleModelConfig;
import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.InnerFitnessNode;
import game.models.ConnectableModel;
import game.models.ModelLearnable;


public abstract class ModelContextBase extends FitnessContextBase {
    protected ConnectableModel bestModel;

    protected Fitness getModelFitness(FitnessNode obj) {
        return getModelFitnessLearnedOnData(obj, learnIndex, validIndex);
    }

    protected Fitness getFitnessOnLearnValid(FitnessNode node) {
        return getModelFitnessLearnedOnData(node, finalLearnIndex, validIndex);
    }

    protected Fitness getModelFitnessLearnedOnData(FitnessNode node, int[] learnIndexes, int[] validIndexes) {
        ModelConfig cfg = (ModelConfig) node;
        ConnectableModel model = new ConnectableModel();

        model.init(cfg, inp, norm);

        ModelLearnable mo = (ModelLearnable) model.getModel();
        mo.setMaxLearningVectors(learnIndexes.length);

        for (int i = 0; i < learnIndexes.length; i++) {
            data.publishVector(learnIndexes[i]);
            model.storeLearningVector(data.getTargetOutput(cfg.getTargetVariable()));
        }
        mo.learn();

        return evaluateModel(model, cfg, validIndexes, testIndex);
    }

    protected double performTestOnData(ConnectableModel model, ModelConfig cfg, int[] dataField) {
        //test on test data
        double RMSE = 0;
        for (int i = 0; i < dataField.length; i++) {
            data.publishVector(dataField[i]);
            RMSE = RMSE + Math.pow(model.getOutput() - data.getTargetOutput(cfg.getTargetVariable()), 2);
        }
        RMSE = Math.sqrt(RMSE / dataField.length);

        return -1 * RMSE;
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
        if (node instanceof EnsembleModelConfig) coef = ((EnsembleModelConfig) node).getModelsNumber();

        return sum / node.getNodesNumber() * coef;
    }

    protected Fitness evaluateModel(ConnectableModel model, ModelConfig cfg, int[] validIndex, int[] testIndex) {
        double validFitness = performTestOnData(model, cfg, validIndex);
        double testFitness;
        if (testIndex.length == 0) testFitness = validFitness;
        else testFitness = performTestOnData(model, cfg, testIndex);

        //CRITICAL SECTION
        if (parallelLock != null) getLock();
        if (testFitness > bestFitness) {
            bestFitness = testFitness;
            bestModel = model;
            bestModelConfig = (FitnessNode) cfg;
        }
        if (parallelLock != null) parallelLock.release();
        return new Fitness(validFitness, testFitness);
    }

    /**
     * Set and get functions
     */

    public ConnectableModel getBestModel() {
        return bestModel;
    }

    public FitnessNode getBestModelPredefinedConfig() {
        return (FitnessNode) bestModel.getConfig();
    }

}
