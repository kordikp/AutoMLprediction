package game.clusters;

import game.utils.MyRandom;
import game.utils.Utils;

/**
 * Simple KMeans algorithm
 */
public class ArrayKMeans {
    private double[][] data;
    private int maxIterations;
    private MyRandom rnd;
    private double[][] centers;
    //index of cluster where vector belongs for each vector
    private int[] dataCenter;
    private int dimensions;

    private double clusterSizeMultiplier;

    private int[][] clusterIndexes;
    private int[] vectorsInCluster;

    public ArrayKMeans(double[][] inputData, int numberOfClusters) {
        data = inputData;
        dimensions = data[0].length;
        rnd = new MyRandom(inputData.length);
        if (numberOfClusters > inputData.length) numberOfClusters = inputData.length;
        centers = new double[numberOfClusters][dimensions];
        maxIterations = 100;
        dataCenter = new int[data.length];
        clusterSizeMultiplier = 1;
    }

    public void setClusterSizeMultiplier(double multiplier) {
        if (multiplier <= 1) this.clusterSizeMultiplier = 1;
        else this.clusterSizeMultiplier = multiplier;
    }

    public void setMaxiterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void run() {
        initRandomCenters();

        boolean changed;
        int rounds = 0;
        int tmpIndex;
        for (int k = 0; k < maxIterations; k++) {
            changed = false;
            for (int i = 0; i < data.length; i++) {
                tmpIndex = findClosestCenter(data[i]);
                if (tmpIndex != dataCenter[i]) {
                    dataCenter[i] = tmpIndex;
                    changed = true;
                }
            }
            if (changed == false) {
                rounds++;
                if (rounds == 3) break;
            } else rounds = 0;

            recomputeCenterPositions();
        }

        if (clusterSizeMultiplier <= 1) computeClusterIndexes();
        else computeClusterIndexesWithMore();

        removeEmptyClusters();
    }

    public double[][] getCentroids() {
        return centers;
    }

    public int[][] getMemberIndexes() {
        return clusterIndexes;
    }

    public void removeEmptyClusters() {
        int nonEmptyClusters = 0;
        for (int i = 0; i < vectorsInCluster.length; i++) {
            if (vectorsInCluster[i] != 0) nonEmptyClusters++;
        }

        double[][] tmpCenters = new double[nonEmptyClusters][dimensions];
        int[][] tmpClusterIndexes = new int[nonEmptyClusters][];

        int idx = 0;
        for (int i = 0; i < vectorsInCluster.length; i++) {
            if (vectorsInCluster[i] != 0) {
                System.arraycopy(centers[i], 0, tmpCenters[idx], 0, dimensions);
                tmpClusterIndexes[idx] = new int[clusterIndexes[i].length];
                System.arraycopy(clusterIndexes[i], 0, tmpClusterIndexes[idx], 0, clusterIndexes[i].length);
                idx++;
            }
        }
        centers = tmpCenters;
        clusterIndexes = tmpClusterIndexes;
    }

    private void computeClusterIndexes() {
        clusterIndexes = new int[centers.length][];
        int[] indexes = new int[centers.length];
        for (int i = 0; i < centers.length; i++) {
            clusterIndexes[i] = new int[vectorsInCluster[i]];
        }

        for (int i = 0; i < dataCenter.length; i++) {
            clusterIndexes[dataCenter[i]][indexes[dataCenter[i]]] = i;
            indexes[dataCenter[i]]++;
        }
    }

    private void computeClusterIndexesWithMore() {
        int[][] tmpIndexes = new int[centers.length][data.length];
        int[] clusterIdx = new int[centers.length];

        for (int i = 0; i < dataCenter.length; i++) {
            tmpIndexes[dataCenter[i]][clusterIdx[dataCenter[i]]] = i;
            clusterIdx[dataCenter[i]]++;
        }

        double[] distanceToCenters = new double[centers.length];
        int[] indexes = new int[centers.length];
        double closestDistance;

        double[][] centersDistance = getCentersDistance();
        for (int i = 0; i < data.length; i++) {

            for (int j = 0; j < distanceToCenters.length; j++) distanceToCenters[j] = 0;

            for (int j = 0; j < centers.length; j++) {
                for (int k = 0; k < dimensions; k++) {
                    distanceToCenters[j] += Math.pow(data[i][k] - centers[j][k], 2);
                }
                distanceToCenters[j] = Math.sqrt(distanceToCenters[j]);
            }

            for (int j = 0; j < centers.length; j++) indexes[j] = j;
            Utils.quicksort(distanceToCenters, indexes, 0, distanceToCenters.length - 1);

            closestDistance = distanceToCenters[indexes[0]];
            for (int j = 1; j < centers.length; j++) {
                //if distance is greater than distance between centers, ignore that vector
                if (distanceToCenters[indexes[j]] > centersDistance[indexes[j]][indexes[0]]) {
                    continue;
                }
                if (distanceToCenters[indexes[j]] / closestDistance < clusterSizeMultiplier) {
                    //} if (closestDistance/distanceToCenters[indexes[j]] > rnd.nextDouble()) {
                    //indexes[j] - cluster where vector i belongs too
                    tmpIndexes[indexes[j]][clusterIdx[indexes[j]]] = i;
                    clusterIdx[indexes[j]]++;
                } else break;
            }
        }

        clusterIndexes = new int[centers.length][];
        for (int i = 0; i < centers.length; i++) {
            clusterIndexes[i] = new int[clusterIdx[i]];
            System.arraycopy(tmpIndexes[i], 0, clusterIndexes[i], 0, clusterIndexes[i].length);
        }
    }

    private double[][] getCentersDistance() {
        double[][] centersDistance = new double[centers.length][centers.length];
        double distance;
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers.length; j++) {
                if (i == j) continue;
                distance = 0;
                for (int k = 0; k < dimensions; k++) {
                    distance += Math.pow(centers[i][k] - centers[j][k], 2);
                }
                centersDistance[i][j] = Math.sqrt(distance);
            }
        }
        return centersDistance;
    }

    private void recomputeCenterPositions() {
        int[] numberOfPoints = new int[centers.length];
        double[][] sum = new double[centers.length][dimensions];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < dimensions; j++) {
                sum[dataCenter[i]][j] += data[i][j];
            }
            numberOfPoints[dataCenter[i]]++;
        }

        for (int i = 0; i < sum.length; i++) {
            for (int j = 0; j < dimensions; j++) {
                if (numberOfPoints[i] != 0) centers[i][j] = sum[i][j] / numberOfPoints[i];
            }
        }
        vectorsInCluster = numberOfPoints;
    }

    private int findClosestCenter(double[] vector) {
        double distance = Double.POSITIVE_INFINITY;
        int index = 0;
        double tmpDist;
        for (int i = 0; i < centers.length; i++) {
            tmpDist = 0;
            for (int j = 0; j < vector.length; j++) {
                tmpDist += Math.pow(vector[j] - centers[i][j], 2);
            }

            if (tmpDist < distance) {
                distance = tmpDist;
                index = i;
            }
        }
        return index;
    }

    private void initRandomCenters() {
        int rndIdx;
        for (int i = 0; i < centers.length; i++) {
            rndIdx = rnd.getRandom(data.length);
            System.arraycopy(data[rndIdx], 0, centers[i], 0, dimensions);
        }
    }

}
