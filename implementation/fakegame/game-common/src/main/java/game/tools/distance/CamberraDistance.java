package game.tools.distance;


import java.util.Arrays;

public class CamberraDistance extends DistanceMeasure {

    public CamberraDistance(double[][] vectors) {
        super(vectors);
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = new double[nearestVectors];
        int[] indexes = new int[nearestVectors];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);

        double v1;
        double v2;
        double distanceSum;
        int lastIndex = distances.length-1;
        double[] currentVector;
        outerLoop:
        for (int i = 0; i < vectors.length; i++) {
            distanceSum=0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                v1 = vector[j];
                v2 = currentVector[j];
                if (v1 == 0 && v2 == 0) continue;
                else distanceSum = distanceSum + Math.abs(v1 - v2) / (Math.abs(v1) + Math.abs(v2));
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
        double[] distances = new double[vectors.length];

        double v1;
        double v2;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                v1 = vector[j];
                v2 = currentVector[j];
                if (v1 == 0 && v2 == 0) continue;
                else distances[i] = distances[i] + Math.abs(v1 - v2) / (Math.abs(v1) + Math.abs(v2));
            }

        }
        return distances;
    }

}
