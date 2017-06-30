package game.evolution.treeEvolution.context;

import game.evolution.treeEvolution.evolutionControl.AreaDataDivide;

/**
 * Context using area divide based on distances between vectors
 */
public class AreaDivideModelContext extends LearnTestModelContext {

    protected void divideLearnData(int[] indexes) {
        double[][] rawDataIn = data.getInputVectors();

        if (indexes.length != rawDataIn.length) {
            double[][] tmpDataIn = new double[indexes.length][];
            for (int i = 0; i < indexes.length; i++) {
                tmpDataIn[i] = rawDataIn[indexes[i]];
            }
            rawDataIn = tmpDataIn;
        }

        int[] classes = new int[rawDataIn.length];

        AreaDataDivide divide = new AreaDataDivide();
        divide.init(rawDataIn, classes);
        //ratios for data in that order: valid,learn,test
        int[][] dataIndexes;
        if (testDataPercent == 0) dataIndexes = divide.divide(new int[]{1, 4, 0});
        else dataIndexes = divide.divide(new int[]{1, 3, 1});
        //remap divided indexes into complete data space
        if (indexes.length != data.getInstanceNumber()) {
            for (int i = 0; i < dataIndexes.length; i++) {
                for (int j = 0; j < dataIndexes[i].length; j++) {
                    dataIndexes[i][j] = indexes[dataIndexes[i][j]];
                }
            }
        }

        learnIndex = dataIndexes[1];
        validIndex = dataIndexes[0];
        testIndex = dataIndexes[2];
        finalLearnIndex = mergeArrays(learnIndex, validIndex);
    }

}
