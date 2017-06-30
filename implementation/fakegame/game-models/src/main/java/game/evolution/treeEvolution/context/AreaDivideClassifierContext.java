package game.evolution.treeEvolution.context;

import game.evolution.treeEvolution.evolutionControl.AreaDataDivide;

/**
 * Context for classifiers
 */
public class AreaDivideClassifierContext extends LearnTestClassifierContext {

    protected void divideLearnData(int[] indexes) {
        double[][] rawDataIn = data.getInputVectors();
        double[][] rawDataOut = data.getOutputAttrs();

        if (indexes.length != rawDataIn.length) {
            double[][] tmpDataIn = new double[indexes.length][];
            double[][] tmpDataOut = new double[indexes.length][];
            for (int i = 0; i < indexes.length; i++) {
                tmpDataIn[i] = rawDataIn[indexes[i]];
                tmpDataOut[i] = rawDataOut[indexes[i]];
            }
            rawDataIn = tmpDataIn;
            rawDataOut = tmpDataOut;
        }

        int[] classes = new int[rawDataIn.length];
        for (int i = 0; i < rawDataIn.length; i++) {
            classes[i] = getMaxIndex(rawDataOut[i]);
        }

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

    protected static int getMaxIndex(double[] array) {
        int maxIndex = 0;
        double maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
