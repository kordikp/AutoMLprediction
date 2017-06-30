package game.tools.distance;


import java.util.Arrays;

public class ManhattanDistance extends DistanceMeasure {

    public ManhattanDistance(double[][] vectors) {
        super(vectors);
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = new double[nearestVectors];
        int[] indexes = new int[nearestVectors];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);

        double sum;
        int lastIndex = distances.length-1;
        double[] currentVector;
        outerLoop:
        for (int i = 0; i < vectors.length; i++) {
            sum = 0.0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                sum += Math.abs(vector[j] - currentVector[j]);
                if (sum >= distances[lastIndex]) {
                    continue outerLoop;
                }
            }
             addToSortedArray(distances, sum,indexes,i);
        }
        return new DistancesWithIndexes(distances,indexes);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double sum;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            sum = 0.0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                sum += Math.abs(vector[j] - currentVector[j]);
            }
            distances[i] = sum;
        }
        return distances;
    }

}
