package game.tools.distance;


import java.util.Arrays;

public class EuclideanDistance extends DistanceMeasure {

    public EuclideanDistance(double[][] vectors) {
        super(vectors);
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = new double[nearestVectors];
        int[] indexes = new int[nearestVectors];
        Arrays.fill(distances,Double.POSITIVE_INFINITY);

        double distance;
        double distanceSum;
        int lastIndex = distances.length-1;
        double[] currentVector;
        outerLoop:
        for (int i = 0; i < vectors.length; i++) {
            distanceSum = 0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                distance = vector[j] - currentVector[j];
                distanceSum += distance*distance;
                if (distanceSum >= distances[lastIndex]) {
                    continue outerLoop;
                }
            }
            addToSortedArray(distances, distanceSum,indexes,i);
        }

        for (int i = 0; i < distances.length; i++) {
            distances[i] = Math.sqrt(distances[i]);
        }
        return new DistancesWithIndexes(distances,indexes);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double distance;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                distance = vector[j] - currentVector[j];
                distances[i] += distance*distance;
            }
            distances[i] = Math.sqrt(distances[i]);
        }
        return distances;
    }
}
