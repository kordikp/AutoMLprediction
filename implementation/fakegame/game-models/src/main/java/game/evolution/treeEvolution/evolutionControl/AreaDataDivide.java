package game.evolution.treeEvolution.evolutionControl;

import java.util.ArrayList;


public class AreaDataDivide {
    protected double[][] inputs;
    protected int[] classes;

    protected double computeDistance(double[] input1, double[] input2) {
        int sum = 0;
        for (int i = 0; i < input1.length; i++) {
            sum += Math.pow(input1[i] - input2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public void init(double[][] inputs, int[] classes) {
        this.inputs = inputs;
        this.classes = classes;
    }

    protected double[][] computeDistanceMatrix(double[][] inputs, int[] indexes) {
        double[][] distanceMatrix = new double[indexes.length][indexes.length];
        double distance;
        for (int i = 0; i < distanceMatrix.length - 1; i++) {
            for (int j = i + 1; j < distanceMatrix.length; j++) {
                distance = computeDistance(inputs[indexes[i]], inputs[indexes[j]]);

                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }
        return distanceMatrix;
    }

    protected int sum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    protected int[] getNearestNeighbours(double[] data, ArrayList<Integer> allowedIndex, int firstN) {
        firstN = Math.min(firstN, allowedIndex.size());
        int[] output = new int[firstN];
        output[0] = allowedIndex.get(0);
        int j;
        //init output array
        for (int i = 1; i < firstN; i++) {
            for (j = 0; j < i; j++) {
                if (data[allowedIndex.get(i)] < data[output[j]]) {
                    shiftRight(j, output);
                    output[j] = allowedIndex.get(i);
                    break;
                }
            }
            if (j == i) output[j] = allowedIndex.get(i);
        }

        int lastIndex = firstN - 1;
        for (int i = firstN; i < allowedIndex.size(); i++) {
            if (data[allowedIndex.get(i)] >= data[output[lastIndex]]) continue;

            for (j = lastIndex - 1; j >= 0; j--) {
                if (data[allowedIndex.get(i)] >= data[output[j]]) {
                    shiftRight(j + 1, output);
                    output[j + 1] = allowedIndex.get(i);
                    break;
                }
            }
            if (j < 0) {
                shiftRight(0, output);
                output[0] = allowedIndex.get(i);
            }
        }
        return output;
    }

    private static void shiftRight(int fromIndex, int[] array) {
        for (int i = array.length - 2; i >= fromIndex; i--) {
            array[i + 1] = array[i];
        }
    }


    public int[][] divide(int[] divideRatios) {
        //prepare array for sets size ratios
        int numberOfSets = divideRatios.length;

        int ratiosSum = sum(divideRatios);
        int[] setIndex = new int[ratiosSum];
        int idx = 0;
        int oldIdx = -1;
        //choose indexes this way to prevent long sequences of the same index
        while (idx != oldIdx) {
            oldIdx = idx;
            for (int i = 0; i < numberOfSets; i++) {
                if (divideRatios[i] > 0) {
                    setIndex[idx] = i;
                    divideRatios[i]--;
                    idx++;
                }
            }
        }

        int[][] indexes = radixSort(classes);

        ArrayList<Integer>[][] dividedData = new ArrayList[indexes.length][];
        for (int i = 0; i < indexes.length; i++) {
            dividedData[i] = performDivide(computeDistanceMatrix(inputs, indexes[i]), indexes[i], setIndex, numberOfSets);
        }

        //merge values for corresponding parts
        ArrayList<Integer>[] mergedData = new ArrayList[numberOfSets];
        for (int i = 0; i < mergedData.length; i++) mergedData[i] = new ArrayList<Integer>();

        for (int i = 0; i < dividedData.length; i++) { // = for all outputs
            for (int j = 0; j < dividedData[i].length; j++) { // = for all sets
                for (int k = 0; k < dividedData[i][j].size(); k++) { // = for all indexes
                    mergedData[j].add(dividedData[i][j].get(k));
                }
            }
        }

        //convert into array
        int[][] finalIndexes = new int[mergedData.length][];
        for (int i = 0; i < mergedData.length; i++) {
            finalIndexes[i] = new int[mergedData[i].size()];
            for (int j = 0; j < mergedData[i].size(); j++) {
                finalIndexes[i][j] = mergedData[i].get(j);
            }
        }
        return finalIndexes;
    }

    protected int[][] radixSort(int[] data) {
        int parts = max(data) + 1;
        ArrayList<Integer>[] partIndexes = new ArrayList[parts];
        for (int i = 0; i < partIndexes.length; i++) partIndexes[i] = new ArrayList<Integer>();

        for (int i = 0; i < data.length; i++) {
            partIndexes[data[i]].add(i);
        }

        int[][] indexes = new int[partIndexes.length][];
        for (int i = 0; i < partIndexes.length; i++) {
            indexes[i] = new int[partIndexes[i].size()];
            for (int j = 0; j < partIndexes[i].size(); j++) {
                indexes[i][j] = partIndexes[i].get(j);
            }
        }
        return indexes;
    }

    protected ArrayList<Integer>[] performDivide(double[][] distanceMatrix, int[] indexes, int[] setIndex, int numberOfSets) {
        ArrayList<Integer> indexesAvailable = new ArrayList<Integer>();
        for (int i = 0; i < distanceMatrix.length; i++) indexesAvailable.add(i);

        ArrayList<Integer>[] outputIndexes = new ArrayList[numberOfSets];
        for (int i = 0; i < outputIndexes.length; i++) outputIndexes[i] = new ArrayList<Integer>();

        int[] nearestNeighbours;
        int setIdx = 0;
        while (!indexesAvailable.isEmpty()) {
            nearestNeighbours = getNearestNeighbours(distanceMatrix[indexesAvailable.get(0)], indexesAvailable, numberOfSets);
            for (int j = 0; j < nearestNeighbours.length; j++) {
                outputIndexes[setIndex[setIdx]].add(indexes[nearestNeighbours[j]]);
                setIdx = (setIdx + 1) % setIndex.length;

                indexesAvailable.remove((Object) nearestNeighbours[j]);
            }
        }

        return outputIndexes;
    }


    protected int max(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

}
