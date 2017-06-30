package game.evolution.treeEvolution.evolutionControl;

import game.data.AbstractGameData;
import game.data.ArrayGameData;
import game.utils.Utils;
import org.apache.log4j.Logger;


public class PreprocessingControl {

    Logger log;
    int[] reducedIndexes;
    int[] discardedIndexes;

    public PreprocessingControl() {
        log = Logger.getLogger(this.getClass());
    }

    public AbstractGameData run(AbstractGameData oldData, int instanceReduceThreshold, int inputReduceThreshold) {
        int[] reducedIndexes = null;
        double[][] rawDataIn = oldData.getInputVectors();
        double[][] rawDataOut = oldData.getOutputAttrs();

        //INSTANCE REDUCE
        if (oldData.getInstanceNumber() > instanceReduceThreshold) {
            reducedIndexes = reduceData(instanceReduceThreshold, rawDataIn, rawDataOut);

            //data that were not chosen
            int[] indexMap = new int[oldData.getInstanceNumber()];
            for (int i = 0; i < reducedIndexes.length; i++) indexMap[reducedIndexes[i]] = 1;

            int idx = 0;
            discardedIndexes = new int[oldData.getInstanceNumber() - reducedIndexes.length];
            for (int i = 0; i < indexMap.length; i++) {
                if (indexMap[i] != 1) {
                    discardedIndexes[idx] = i;
                    idx++;
                }
            }
            this.reducedIndexes = reducedIndexes;

            rawDataIn = filterData(reducedIndexes, rawDataIn);
            rawDataOut = filterData(reducedIndexes, rawDataOut);
        }
        //INPUT REDUCE
        if (oldData.getINumber() > inputReduceThreshold) {
            rawDataIn = Utils.transpose(rawDataIn);

            reducedIndexes = reduceInputs(inputReduceThreshold, rawDataIn);
            rawDataIn = filterData(reducedIndexes, rawDataIn);
            rawDataIn = Utils.transpose(rawDataIn);
            //no instance reduction applied
            if (reducedIndexes == null) {
                this.reducedIndexes = new int[rawDataIn.length];
                for (int i = 0; i < reducedIndexes.length; i++) reducedIndexes[i] = i;
            }
        }

        //todo: outlayer detection, prazdne hodnoty atd..
        if (reducedIndexes == null) {
            return oldData;
        } else {
            ArrayGameData data = new ArrayGameData(rawDataIn, rawDataOut);
            return data;
        }
    }

    public int[] getDiscardedIndexes() {
        return discardedIndexes;
    }

    public int[] getReducedIndexes() {
        return reducedIndexes;
    }

    protected double[][] filterData(int[] filterIndexes, double[][] data) {
        double[][] filteredData = new double[filterIndexes.length][data[0].length];
        for (int i = 0; i < filterIndexes.length; i++) {
            System.arraycopy(data[filterIndexes[i]], 0, filteredData[i], 0, data[0].length);
        }
        return filteredData;
    }

    protected int[] reduceInputs(int reduceThreshold, double[][] rawDataIn) {
        HistogramDataReduce reduce = new HistogramDataReduce();
        reduce.init(rawDataIn, new double[0][0]);
        int[] reducedIndexes = reduce.inputClusterReduce(reduceThreshold);
        log.info("INPUTS REDUCED FROM " + rawDataIn.length + " TO " + reducedIndexes.length);
        return reducedIndexes;
    }

    protected int[] reduceData(int reduceThreshold, double[][] rawDataIn, double[][] rawDataOut) {
        HistogramDataReduce reduce = new HistogramDataReduce();
        reduce.init(rawDataIn, rawDataOut);
        int[] reducedIndexes = reduce.clusterReduce(reduceThreshold);
        log.info("DATA REDUCED FROM " + rawDataIn.length + " TO " + reducedIndexes.length);
        return reducedIndexes;
    }
}
