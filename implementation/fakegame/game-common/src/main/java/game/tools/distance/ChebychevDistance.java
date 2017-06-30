package game.tools.distance;


import java.util.Arrays;

public class ChebychevDistance extends DistanceMeasure {

    public ChebychevDistance(double[][] vectors) {
        super(vectors);
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = new double[nearestVectors];
        int[] indexes = new int[nearestVectors];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);

        double distance;
        double distanceSum;
        int lastIndex = distances.length-1;
        double[] currentVector;
        outerLoop:
        for (int i = 0; i < vectors.length; i++) {
            distanceSum=0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                distance = Math.abs(vector[j] - currentVector[j]);

                if (distance > distanceSum) distanceSum = distance;
                if (distanceSum >= distances[lastIndex]) {
                    continue outerLoop;
                }
            }
            addToSortedArray(distances, distanceSum,indexes,i);
        }
        return new DistancesWithIndexes(distances,indexes);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        //distance is always positive, so leave init values to 0
        double[] distances = new double[vectors.length];

        double distance;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                distance = Math.abs(vector[j] - currentVector[j]);

                if (distance > distances[i]) distances[i] = distance;
            }
        }
        return distances;
    }

}
