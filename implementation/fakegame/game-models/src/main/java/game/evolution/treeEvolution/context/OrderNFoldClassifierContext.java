package game.evolution.treeEvolution.context;

import game.classifiers.ConnectableClassifier;
import game.utils.Utils;


public class OrderNFoldClassifierContext extends NFoldClassifierContext {
    protected double performTestOnData(ConnectableClassifier cls, int[] dataField) {
        double[] chosenClass;
        double[] rightClass;
        double fitness = 0;

        for (int i = 0; i < dataField.length; i++) {
            data.publishVector(dataField[i]);

            chosenClass = cls.getOutputProbabilities();
            rightClass = data.getTargetOutputs();
            //initialize indexes
            int[] rightIndexes = new int[rightClass.length];
            for (int j = 0; j < rightIndexes.length; j++) rightIndexes[j] = j;

            int[] chosenIndexes = new int[chosenClass.length];
            for (int j = 0; j < chosenIndexes.length; j++) chosenIndexes[j] = j;

            Utils.quicksort(rightClass, rightIndexes, 0, rightIndexes.length - 1);
            Utils.quicksort(chosenClass, chosenIndexes, 0, chosenIndexes.length - 1);

            fitness -= arrayDiff(rightIndexes, chosenIndexes);
        }
        return fitness / dataField.length;
    }

    protected int arrayDiff(int[] array1, int[] array2) {
        int diff = 0;
        for (int i = 0; i < array1.length; i++) {
            diff += Math.abs(array1[i] - array2[i]);
        }
        return diff;
    }

}
