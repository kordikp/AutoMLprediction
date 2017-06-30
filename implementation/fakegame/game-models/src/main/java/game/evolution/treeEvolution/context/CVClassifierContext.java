package game.evolution.treeEvolution.context;

import configuration.classifiers.ClassifierConfig;
import game.classifiers.ConnectableClassifier;
import game.data.AbstractGameData;
import game.evolution.treeEvolution.FitnessNode;
import game.utils.MyRandom;

/**
 * cross validation fitness
 */
public class CVClassifierContext extends ClassifierContextBase {

    protected int folds = 3;
    protected int[][] foldsIndex;

    protected Fitness getModelFitness(FitnessNode obj) {
        ClassifierConfig cfg = (ClassifierConfig) obj;
        ConnectableClassifier cls = new ConnectableClassifier();

        Fitness fitness = new Fitness();
        Fitness foldFitness;
        for (int i = 0; i < foldsIndex.length; i++) {
            cls.init(cfg, inp, norm);
            cls.setMaxLearningVectors(dataNum - foldsIndex[i].length);

            for (int j = 0; j < foldsIndex.length; j++) {
                if (i == j) continue; //VALID DATA
                for (int k = 0; k < foldsIndex[j].length; k++) {
                    data.publishVector(foldsIndex[j][k]);
                    cls.storeLearningVector(data.getTargetOutputs());
                }
            }
            cls.learn();

            foldFitness = evaluateClassifier(cls, obj, foldsIndex[i], testIndex);

            fitness.validFitness += foldFitness.validFitness;
            fitness.testFitness += foldFitness.testFitness;
        }
        fitness.validFitness = fitness.validFitness / foldsIndex.length;
        fitness.testFitness = fitness.testFitness / foldsIndex.length;

        return fitness;
    }

    protected void divideLearnData(int[] indexes) {
        int dataNum = indexes.length;
        MyRandom rnd = new MyRandom(dataNum);
        //TEST DATA SAMPLE
        testIndex = new int[(int) (dataNum * testDataPercent)];
        for (int i = 0; i < testIndex.length; i++) {
            testIndex[i] = indexes[rnd.getRandom(dataNum)];
        }
        int remainingData = dataNum - testIndex.length;

        finalLearnIndex = new int[remainingData];

        int foldSize;
        if (folds == -1) foldsIndex = new int[dataNum - testIndex.length][];
        else foldsIndex = new int[folds][];
        for (int i = foldsIndex.length; i > 0; i--) {
            foldSize = (int) Math.ceil(remainingData / i);
            foldsIndex[i - 1] = new int[foldSize];
            remainingData -= foldSize;
        }

        int idx = 0;
        for (int i = 0; i < foldsIndex.length; i++) {
            for (int j = 0; j < foldsIndex[i].length; j++) {
                foldsIndex[i][j] = indexes[rnd.getRandom(dataNum)];
                finalLearnIndex[idx] = foldsIndex[i][j];
                idx++;
            }
        }
    }

    public void init(AbstractGameData data, int[] inputLearnValidIndex, int[] inputTestIndex) {
        initContextVariables(data);

        double oldTestDataPercent = testDataPercent;
        int[][] foldsIdx = new int[0][0];
        if (inputLearnValidIndex.length > 0) {
            testDataPercent = 0;
            divideLearnData(inputLearnValidIndex);
            foldsIdx = foldsIndex;
        }

        //recompute test data percent to match exactly given ratio if input learn and test indexes does not have desired ratio
        int[] addedIndexes = getComplementOfSets(inputLearnValidIndex, inputTestIndex);
        testDataPercent = (oldTestDataPercent * data.getInstanceNumber() - inputTestIndex.length) / addedIndexes.length;
        divideLearnData(addedIndexes);
        testDataPercent = oldTestDataPercent;
        //merge data
        for (int i = 0; i < foldsIndex.length; i++) {
            foldsIndex[i] = mergeArrays(foldsIdx[i], foldsIndex[i]);
        }
        testIndex = mergeArrays(inputTestIndex, testIndex);
        finalLearnIndex = mergeArrays(inputLearnValidIndex, finalLearnIndex);
    }


    public void setFoldsNumber(int folds) {
        this.folds = folds;
    }
}
