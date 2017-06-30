package game.evolution.treeEvolution.evolutionControl;

import game.utils.MyRandom;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Reduces data based on histogram of its output/input values
 */
public class HistogramDataReduce {
    protected double[][] inputs;
    protected double[][] outputs;
    Logger log;

    public void init(double[][] inputs, double[][] outputs) {
        this.inputs = copyArray(inputs);
        this.outputs = copyArray(outputs);
        log = Logger.getLogger(this.getClass());
    }

    public int[] outputClusterReduce(int reduceTo) {
        return doReduce(reduceTo, false, true);
    }

    public int[] inputClusterReduce(int reduceTo) {
        return doReduce(reduceTo, true, false);
    }

    public int[] clusterReduce(int reduceTo) {
        return doReduce(reduceTo, true, true);
    }

    public int[] randomReduce(int reduceTo) {
        int[] chosenIndexes = new int[reduceTo];
        MyRandom rndGen = new MyRandom(inputs.length);
        for (int i = 0; i < chosenIndexes.length; i++) {
            chosenIndexes[i] = rndGen.nextInt(inputs.length);
        }
        return chosenIndexes;
    }

    private Hashtable<String, ArrayList<Integer>> clusterize(int reduceTo, boolean useInputs, boolean useOutputs) {
        int dimensions = 0;
        int instances = 0;
        if (useInputs) {
            normalize(inputs);
            dimensions += inputs[0].length;
            instances = inputs.length;
        }
        if (useOutputs) {
            normalize(outputs);
            dimensions += outputs[0].length;
            instances = outputs.length;
        }

        double root = 1 / (Math.sqrt(dimensions) + 1);
        double intervals = Math.pow((double) instances, root);

        Hashtable<String, ArrayList<Integer>> histogram = null;
        int coef = 2;
        int diff = 1;
        boolean binaryDiv = false;
        int prevOp = 2;
        for (int i = 0; i < 10; i++) {
            histogram = performClusterization(intervals, useInputs, useOutputs);
            log.debug("CLUSTERS: " + histogram.size());
            if (histogram.size() < reduceTo / 2) { //increase interval num
                if (!binaryDiv && prevOp == 1) {
                    binaryDiv = true;
                    continue;
                } else {
                    intervals = intervals * coef / (coef - 1);
                    prevOp = 0;
                }
            } else if (histogram.size() > reduceTo * 2) { //decrease interval num
                if (!binaryDiv && prevOp == 0) {
                    binaryDiv = true;
                    continue;
                } else {
                    intervals = intervals * (coef - 1) / coef;
                    prevOp = 1;
                }
            } else {
                break;
            }

            if (binaryDiv) {
                coef += diff;
                diff = diff * 2;
            }
        }


        return histogram;
    }

    private void normalize(double[][] data) {
        for (int i = 0; i < data[0].length; i++) {
            double max = data[0][i];
            double min = data[0][i];
            for (int j = 1; j < data.length; j++) {
                if (data[j][i] > max) max = data[j][i];
                else if (data[j][i] < min) min = data[j][i];
            }

            double diff = max - min;
            for (int j = 0; j < data.length; j++) {
                data[j][i] = (data[j][i] - min) / diff;
            }
        }
    }

    private Hashtable<String, ArrayList<Integer>> performClusterization(double intervals, boolean useInputs, boolean useOutputs) {
        //index are interval indexes in each dimension, values are indexes of data
        Hashtable<String, ArrayList<Integer>> histogram = new Hashtable<String, ArrayList<Integer>>();

        int instances;
        if (useInputs) instances = inputs.length;
        else instances = outputs.length;
        String index;
        for (int i = 0; i < instances; i++) {
            index = "";
            if (useInputs) index += getIntervalIndexes(inputs[i], intervals);
            if (useOutputs) index += getIntervalIndexes(outputs[i], intervals);

            if (histogram.containsKey(index)) {
                histogram.get(index).add(i);
            } else {
                ArrayList<Integer> newItem = new ArrayList<Integer>();
                newItem.add(i);
                histogram.put(index, newItem);
            }
        }
        return histogram;
    }

    private int[] doReduce(int reduceTo, boolean useInputs, boolean useOutputs) {
        Hashtable<String, ArrayList<Integer>> histogram = clusterize(reduceTo, useInputs, useOutputs);

        Enumeration<String> keys = histogram.keys();
        ArrayList<String> hashIndexes = new ArrayList<String>();
        while (keys.hasMoreElements()) {
            hashIndexes.add(keys.nextElement());
        }

        Random rndWithRep = new Random(System.nanoTime());
        int[] chosenIndexes = new int[reduceTo];
        int instanceIndex;
        int clusterIndex = 0;
        ArrayList<Integer> cluster;
        Collections.shuffle(hashIndexes);
        for (int i = 0; i < chosenIndexes.length; i++) {
            cluster = histogram.get(hashIndexes.get(clusterIndex));
            //choose with repetition among instances in cluster
            instanceIndex = rndWithRep.nextInt(cluster.size());
            chosenIndexes[i] = cluster.get(instanceIndex);
            //remove chosen instance from further selection
            cluster.remove(instanceIndex);
            //remove cluster if its empty
            if (cluster.isEmpty()) {
                hashIndexes.remove(clusterIndex);
            } else {
                clusterIndex++;
            }
            clusterIndex = clusterIndex % hashIndexes.size();
        }

        return chosenIndexes;
    }

    private String getIntervalIndexes(double[] data, double intervals) {
        String output = "";
        double remainder = 0;
        double decFraction = intervals - (int) intervals;
        for (int i = 0; i < data.length; i++) {
            output += Integer.toString((int) (data[i] * (int) (intervals + remainder))) + ";";
            remainder += decFraction;
            if (remainder > 1) remainder--;
        }
        return output;
    }

    private double[][] copyArray(double[][] array) {
        if (array.length == 0) return array;
        double[][] duplicate = new double[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, duplicate[i], 0, array[0].length);
        }
        return duplicate;
    }


}
