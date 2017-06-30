package game.tools.distance;


//import game.utils.Utils;

import java.io.Serializable;

public abstract class DistanceMeasure implements Serializable{

    protected double[][] vectors;

    public abstract double[] getDistanceToAll(double[] vector);

    public DistanceMeasure(double[][] vectors) {
        this.vectors = vectors;
    }

    private static int[] insertSort(double[] data, int firstN) {
        if (data.length == 0) return new int[0];
        else if (firstN > data.length) firstN = data.length;

        int[] output = new int[firstN];
        output[0] = 0;
        int j;
        //init output array
        for (int i=1;i<firstN;i++) {
            for (j=0;j<i;j++) {
                if (data[i] < data[output[j]]) {
                    shiftRight(j,output);
                    output[j] = i;
                    break;
                }
            }
            if (j==i) output[j] = i;
        }

        int lastIndex = firstN-1;
        for (int i=firstN;i<data.length;i++) {
            if (data[i] >= data[output[lastIndex]]) continue;

            for (j=lastIndex-1;j>=0;j--) {
                if (data[i] >= data[output[j]]) {
                    shiftRight(j+1, output);
                    output[j+1] = i;
                    break;
                }
            }
            if (j<0) {
                shiftRight(0, output);
                output[0] = i;
            }
        }
        return output;
    }

    private static void shiftRight(int fromIndex, int[] array) {
        for (int i=array.length-2;i>=fromIndex;i--) {
            array[i+1] = array[i];
        }
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = getDistanceToAll(vector);
        int[] indexes = insertSort(distances,nearestVectors);

        double[] result = new double[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = distances[indexes[i]];
        }
        return new DistancesWithIndexes(result,indexes);
    }

    protected void addToSortedArrayDesc(double[] sortedArray, double value, int[] indexes, int valueIndex) {
        for (int i = sortedArray.length-1; i > 0; i--) {
            if (sortedArray[i] < value && sortedArray[i-1] >= value) {
                shiftRight(i,sortedArray,indexes);
                sortedArray[i] = value;
                indexes[i] = valueIndex;
                return;
            }
        }

        if (value > sortedArray[0]) {
            shiftRight(0,sortedArray,indexes);
            sortedArray[0] = value;
            indexes[0] = valueIndex;
        }
    }

    protected void addToSortedArray(double[] sortedArray, double value, int[] indexes, int valueIndex) {
        for (int i = sortedArray.length-1; i > 0; i--) {
            if (sortedArray[i] > value && sortedArray[i-1] <= value) {
                shiftRight(i,sortedArray,indexes);
                sortedArray[i] = value;
                indexes[i] = valueIndex;
                return;
            }
        }

        if (value < sortedArray[0]) {
            shiftRight(0,sortedArray,indexes);
            sortedArray[0] = value;
            indexes[0] = valueIndex;
        }
    }

    protected void shiftRight(int fromIndex, double[] array, int[] array2) {
    	for (int i=array.length-2;i>=fromIndex;i--) {
    		array[i+1] = array[i];
            array2[i+1] = array2[i];
    	}
    }

}
