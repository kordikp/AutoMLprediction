package game.evolution.treeEvolution.context;

import game.data.AbstractGameData;
import game.evolution.treeEvolution.FitnessNode;
import game.utils.MyRandom;

import java.util.Random;


public class NFoldClassifierContext extends ClassifierContextBase {

    protected int[][] learnIndex;
    protected int[][] validIndex;
    Random rnd;

    protected Fitness getModelFitness(FitnessNode obj) {
        int rndNum = rnd.nextInt(modelsBeforeCacheUse);
        return getModelFitnessLearnedOnData(obj, learnIndex[rndNum], validIndex[rndNum]);
    }

    protected Fitness getFitnessOnLearnValid(FitnessNode node) {
        int rndNum = rnd.nextInt(modelsBeforeCacheUse);
        return getModelFitnessLearnedOnData(node, finalLearnIndex, validIndex[rndNum]);
    }

    protected void divideLearnData(int[] indexes) {
        //MEMORY ALLOCATION
        int dataNum = indexes.length;
        testIndex = new int[(int) (dataNum * testDataPercent)];
        validIndex = new int[modelsBeforeCacheUse][(int) Math.round(dataNum * validDataPercent)];
        learnIndex = new int[modelsBeforeCacheUse][dataNum - validIndex[0].length - testIndex.length];
        finalLearnIndex = new int[dataNum - testIndex.length];

        //separate test data, they will be always the same
        MyRandom rnd = new MyRandom(dataNum);
        rnd.generateLearningAndTestingSet(testIndex.length);
        testIndex = rnd.getTest();
        finalLearnIndex = rnd.getLearn();

        for (int i = 0; i < testIndex.length; i++) testIndex[i] = indexes[testIndex[i]];
        for (int i = 0; i < finalLearnIndex.length; i++) finalLearnIndex[i] = indexes[finalLearnIndex[i]];

        for (int j = 0; j < modelsBeforeCacheUse; j++) {
            rnd.resetRandom();
            for (int i = 0; i < learnIndex[0].length; i++) {
                learnIndex[j][i] = finalLearnIndex[rnd.getRandom(finalLearnIndex.length)];
            }

            for (int i = 0; i < validIndex[0].length; i++) {
                validIndex[j][i] = finalLearnIndex[rnd.getRandom(finalLearnIndex.length)];
            }
        }
    }

    public void init(AbstractGameData data, int[] inputLearnValidIndex, int[] inputTestIndex) {
        initContextVariables(data);

        double oldTestDataPercent = testDataPercent;
        int[][] newLearnIdx = new int[0][0];
        int[][] newValidIdx = new int[0][0];
        if (inputLearnValidIndex.length > 0) {
            testDataPercent = 0;
            divideLearnData(inputLearnValidIndex);
            newLearnIdx = learnIndex;
            newValidIdx = validIndex;
        }

        //recompute test data percent to match exactly given ratio if input learn and test indexes does not have desired ratio
        int[] addedIndexes = getComplementOfSets(inputLearnValidIndex, inputTestIndex);
        testDataPercent = (oldTestDataPercent * data.getInstanceNumber() - inputTestIndex.length) / addedIndexes.length;
        divideLearnData(addedIndexes);
        testDataPercent = oldTestDataPercent;
        //merge data
        for (int i = 0; i < learnIndex.length; i++) {
            learnIndex[i] = mergeArrays(newLearnIdx[i], learnIndex[i]);
            validIndex[i] = mergeArrays(newValidIdx[i], validIndex[i]);
        }
        testIndex = mergeArrays(inputTestIndex, testIndex);
        finalLearnIndex = mergeArrays(inputLearnValidIndex, finalLearnIndex);
    }

    public void setModelsBeforeCacheUse(int modelsBeforeCacheUse) {
        this.modelsBeforeCacheUse = modelsBeforeCacheUse;
        //do not resize internal arrays if they are not created
        if (finalLearnIndex == null) return;

        //MEMORY ALLOCATION
        int[][] oldValid = validIndex;
        int[][] oldLearn = learnIndex;
        validIndex = new int[modelsBeforeCacheUse][validIndex[0].length];
        learnIndex = new int[modelsBeforeCacheUse][learnIndex[0].length];

        //copy old data
        int min = Math.min(oldLearn.length, learnIndex.length);
        for (int i = 0; i < min; i++) System.arraycopy(oldValid[i], 0, validIndex[i], 0, oldValid[0].length);
        for (int i = 0; i < min; i++) System.arraycopy(oldLearn[i], 0, learnIndex[i], 0, oldLearn[0].length);

        //separate test data, they will be always the same
        MyRandom rnd = new MyRandom(finalLearnIndex.length);
        //fill the new arrays with newly distributed data
        for (int j = oldLearn.length; j < modelsBeforeCacheUse; j++) {
            rnd.resetRandom();
            for (int i = 0; i < learnIndex[0].length; i++) {
                learnIndex[j][i] = finalLearnIndex[rnd.getRandom(finalLearnIndex.length)];
            }

            for (int i = 0; i < validIndex[0].length; i++) {
                validIndex[j][i] = finalLearnIndex[rnd.getRandom(finalLearnIndex.length)];
            }
        }
    }

    protected void initContextVariables(AbstractGameData data) {
        super.initContextVariables(data);
        rnd = new Random(System.nanoTime());
    }

}
