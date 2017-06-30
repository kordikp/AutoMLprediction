package game.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Utils {

    private Utils() {
    }

    public static String convertDouble(double d) {
        String s = Double.toString(d);
        int dotIndex = s.indexOf(".");
        int eIndex = s.indexOf("E");
        if (eIndex > 0) {
            if (eIndex - dotIndex > 4) {
                return (s.substring(0, dotIndex + 4) + s.substring(eIndex, s.length()));
            } else {
                return s;
            }
        }
        if (dotIndex > 0 && dotIndex < s.length() - 4) {
            return (s.substring(0, dotIndex + 4));
        } else {
            return s;
        }
    }

    /**
     * some misplaced function ... propably not used any more:)
     *
     * @param name
     */

    public static String removeSpaces(String name) {
        String noSpaces = name;
        int sp = noSpaces.indexOf(32);
        if (sp > 0) {
            noSpaces = noSpaces.substring(0, sp);
        }
        if (sp == 0) {
            return null;
        }
        if (noSpaces.compareTo("") == 0) {
            return null;
        }
        return noSpaces;
    }


    public static double[] normalizeAndCloneDistribution(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] / sum;
        }
        return result;
    }

    /**
     * Uses serialization for deep copying, it is much slower than creating clone methods for objects. Do not use when
     * you need to copy lots of objects.
     *
     * @param sourceObject Makes deep copy of sourceObject using serialization.
     * @return Copied object.
     * @throws ClassNotFoundException Generated by readObject during deserialization.
     * @throws IOException            Serialization/deserialization.
     */
    public static Object deepCopy(Object sourceObject) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        byte buffer[];
        Object objectCopy = null;
        try {
            // serialize Object into byte array
            oos = new ObjectOutputStream(baos);
            oos.writeObject(sourceObject);
            buffer = baos.toByteArray();
            oos.close();
            // deserialize byte array into Object
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(bais);
            objectCopy = ois.readObject();
            ois.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objectCopy;
    }

    /**
     * Quick sort sorting algorithm implementation. O(n*log(n))
     *
     * @param data    Data array, whose elements are compared during sorting. It does not sort this array.
     * @param indexes Index array(starting value indexes[i] = i i from 0 to data.length-1). This array is sorted in a process.
     * @param low     Interval border variable. Starting value should be 0.
     * @param high    Interval border variable. Starting value should be data.length-1.
     */
    public static void quicksort(double[] data, int[] indexes, int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = data[indexes[(low + high) / 2]];
        int tmpindex;

        while (i <= j) {
            while (data[indexes[i]] < pivot) i++;
            while (data[indexes[j]] > pivot) j--;

            if (i <= j) {
                tmpindex = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpindex;
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quicksort(data, indexes, low, j);
        if (i < high) quicksort(data, indexes, i, high);
    }

    /**
     * Insert sort which is much faster than quicksort, if you want only first few elements from sorted data array.
     *
     * @param data   Data array, whose elements are compared during sorting. It does not sort this array.
     * @param firstN Number of first elements from sorted sequence that will be returned.
     * @return Returns indexes of firstN elements from sorted sequence.
     */
    public static int[] insertSort(double[] data, int firstN) {
        int[] output = new int[firstN];
        output[0] = 0;
        int j;
        //init output array
        for (int i = 1; i < firstN; i++) {
            for (j = 0; j < i; j++) {
                if (data[i] < data[output[j]]) {
                    shiftRight(j, output);
                    output[j] = i;
                    break;
                }
            }
            if (j == i) output[j] = i;
        }

        int lastIndex = firstN - 1;
        for (int i = firstN; i < data.length; i++) {
            if (data[i] >= data[output[lastIndex]]) continue;

            for (j = lastIndex - 1; j >= 0; j--) {
                if (data[i] >= data[output[j]]) {
                    shiftRight(j + 1, output);
                    output[j + 1] = i;
                    break;
                }
            }
            if (j < 0) {
                shiftRight(0, output);
                output[0] = i;
            }
        }
        return output;
    }

    /**
     * Shift elements in array one position to the right, starting from index fromIndex. Value of last element from array is lost.
     *
     * @param fromIndex Index from which to start shift.
     * @param array     Array to be shifted, modifies input array.
     */
    private static void shiftRight(int fromIndex, int[] array) {
        for (int i = array.length - 2; i >= fromIndex; i--) {
            array[i + 1] = array[i];
        }
    }

    /**
     * @param matrix Input matrix.
     * @return Returns transposed input matrix.
     */
    public static double[][] transpose(double[][] matrix) {
        double[][] transposed = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < transposed.length; i++) {
            for (int j = 0; j < transposed[0].length; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }
        return transposed;
    }

    /**
     * Function to compute simple gaussian function. Support flatness change only.
     *
     * @param x        Input value.
     * @param flatness How much is gaussian curve flat. Greater number = more flat.
     * @return Returns gaussian value with gaussian curve maximum fixed in x=0
     */
    public static double gaussian(double x, double flatness) {
        if (flatness == 0) {
            if (x == 0) return 1;
            else return 0;
        } else {
            return Math.exp(-Math.pow(x, 2) / flatness);
        }
    }

    /**
     * @param x         Input value.
     * @param sharpness How sharp or flat the function will be.
     *                  1 - normal sigmoid
     *                  <1 - more flat
     *                  >1 - more sharp
     * @return Returns sigmoid value from target interval (-0.5,0.5)
     */
    public static double sigmoid(double x, double sharpness) {
        return 2.0 / (1.0 + Math.exp(-x * sharpness)) - 1;
    }


    public static int[] quickSort(long[] data) {
        int[] indexes = new int[data.length];
        for (int i = 1; i < indexes.length; i++) indexes[i] = i;

        quickSortIndexes(data, indexes, 0, indexes.length - 1);
        return indexes;
    }

    public static void quickSortIndexes(long[] data, int[] indexes, int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        long pivot = data[indexes[(low + high) / 2]];
        int tmpIndex;

        while (i <= j) {
            while (data[indexes[i]] < pivot) i++;
            while (data[indexes[j]] > pivot) j--;

            if (i <= j) {
                tmpIndex = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpIndex;
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quickSortIndexes(data, indexes, low, j);
        if (i < high) quickSortIndexes(data, indexes, i, high);
    }


    /**
     * quickSort for integer data array
     */
    public static int[] quickSort(int[] data) {
        int[] indexes = new int[data.length];
        for (int i = 1; i < indexes.length; i++) indexes[i] = i;

        quickSortIndexes(data, indexes, 0, indexes.length - 1);
        return indexes;
    }

    public static void quickSortIndexes(int[] data, int[] indexes, int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        int pivot = data[indexes[(low + high) / 2]];
        int tmpIndex;

        while (i <= j) {
            while (data[indexes[i]] < pivot) i++;
            while (data[indexes[j]] > pivot) j--;

            if (i <= j) {
                tmpIndex = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpIndex;
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quickSortIndexes(data, indexes, low, j);
        if (i < high) quickSortIndexes(data, indexes, i, high);
    }


    public static int[] quickSort(double[] data) {
        int[] indexes = new int[data.length];
        for (int i = 1; i < indexes.length; i++) indexes[i] = i;

        quickSortIndexes(data, indexes, 0, indexes.length - 1);
        return indexes;
    }

    /**
     * Quick sort sorting algorithm implementation. O(n*log(n))
     *
     * @param data    Data array, whose elements are compared during sorting. It does not sort this array.
     * @param indexes Index array(starting value indexes[i] = i i from 0 to data.length-1). This array is sorted in a process.
     * @param low     Interval border variable. Starting value should be 0.
     * @param high    Interval border variable. Starting value should be data.length-1.
     */
    public static void quickSortIndexes(double[] data, int[] indexes, int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = data[indexes[(low + high) / 2]];
        int tmpIndex;

        while (i <= j) {
            while (data[indexes[i]] < pivot) i++;
            while (data[indexes[j]] > pivot) j--;

            if (i <= j) {
                tmpIndex = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpIndex;
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quickSortIndexes(data, indexes, low, j);
        if (i < high) quickSortIndexes(data, indexes, i, high);
    }

    public static int[] quickSortDesc(double[] data) {
        int[] indexes = new int[data.length];
        for (int i = 1; i < indexes.length; i++) indexes[i] = i;

        quickSortIndexesDesc(data, indexes, 0, indexes.length - 1);
        return indexes;
    }

    public static void quickSortIndexesDesc(double[] data, int[] indexes, int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = data[indexes[(low + high) / 2]];
        int tmpIndex;

        while (i <= j) {
            while (data[indexes[i]] > pivot) i++;
            while (data[indexes[j]] < pivot) j--;

            if (i <= j) {
                tmpIndex = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpIndex;
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quickSortIndexesDesc(data, indexes, low, j);
        if (i < high) quickSortIndexesDesc(data, indexes, i, high);
    }
}