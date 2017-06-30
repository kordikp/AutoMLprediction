package game.tools.distance;


//import game.tools.FastMath;

import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

public class CosineSimilarity extends DistanceMeasure {

    private double[] sum2;
    private double acosZero;

    public CosineSimilarity(double[][] vectors) {
        super(vectors);

        int dimensions = vectors[0].length;
        sum2 = new double[vectors.length];

        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < dimensions; j++) {
                sum2[i] += vectors[i][j] * vectors[i][j];
            }
            sum2[i] = Math.sqrt(sum2[i]);

        }
        acosZero = FastMath.acos(0d);
    }

    public DistancesWithIndexes getDistanceToNearest(double[] vector, int nearestVectors) {
        double[] distances = new double[nearestVectors];
        int[] indexes = new int[nearestVectors];
        Arrays.fill(distances, Double.NEGATIVE_INFINITY);

        double sum1 = 0;
        for (int j = 0; j < vector.length; j++) {
            sum1 += vector[j]*vector[j];
        }
        sum1 = Math.sqrt(sum1);

        double sum;
        double sum1TimesSum2;
        double distanceSum;
        int lastIndex = distances.length-1;
        double[] currentVector;
        for (int i = 0; i < vectors.length; i++) {
            sum = 0.0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                sum += currentVector[j] * vector[j];
            }

            sum1TimesSum2 = sum1*sum2[i];
            if (sum1TimesSum2 == 0) {
                if (sum1 == 0 && sum2[i] == 0) distanceSum = 1;
                else distanceSum = 0;
            } else {
                distanceSum = sum / sum1TimesSum2;
            }

            if (distanceSum > distances[lastIndex]) {
                addToSortedArrayDesc(distances, distanceSum,indexes,i);
            }
        }

        for (int i = 0; i < distances.length; i++) {
            if (distances[i] == 0) distances[i] = acosZero;
            else if (distances[i] == 1) distances[i] = 0;
            else distances[i] = FastMath.acos(Math.min(Math.max(distances[i], -1d), 1d));
        }
        return new DistancesWithIndexes(distances,indexes);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double sum1 = 0;
        for (int j = 0; j < vector.length; j++) {
            sum1 += vector[j]*vector[j];
        }
        sum1 = Math.sqrt(sum1);

        double sum;
        double result;
        double sum1TimesSum2;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            sum = 0.0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                sum += currentVector[j] * vector[j];
            }

            sum1TimesSum2 = sum1*sum2[i];
            if (sum1TimesSum2 == 0) {
                if (sum1 == 0 && sum2[i] == 0) distances[i] =  0d; //Math.acos(1d)
                else distances[i] = acosZero;
            } else {
                result = sum / sum1TimesSum2;
                // result can be > 1 (or -1) due to rounding errors for equal vectors, but must be between -1 and 1
                distances[i] = FastMath.acos(Math.min(Math.max(result, -1d), 1d));
            }
        }
        return distances;
    }

}

