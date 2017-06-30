package game.tools.distance;


public class CorrelationSimilarity extends DistanceMeasure {

    private double[] sumS2;
    private double[][] meanDiff2;

    public CorrelationSimilarity(double[][] vectors) {
        super(vectors);

        int dimensions = vectors[0].length;
        sumS2 = new double[vectors.length];
        meanDiff2 = new double[vectors.length][dimensions];

        double mean2;
        double[] currentMeanDiff2;
        for (int i = 0; i < vectors.length; i++) {
            // Calculate the mean and stddev
            mean2 = 0.0;

            for (int j = 0; j < dimensions; j++) {
                mean2 += vectors[i][j];
            }
            mean2 = mean2 / dimensions;

            currentMeanDiff2 = meanDiff2[i];
            for (int j = 0; j < dimensions; j++) {
                currentMeanDiff2[j] = vectors[i][j] - mean2;
                sumS2[i] += currentMeanDiff2[j]*currentMeanDiff2[j];
            }
        }
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double mean1 = 0;
        for (int j = 0; j < vector.length; j++) {
            mean1 += vector[j];
        }
        mean1 = mean1/vector.length;

        double sumS1=0;
        double[] meanDiff = new double[vector.length];
        for (int j = 0; j < vector.length; j++) {
            meanDiff[j] = vector[j] - mean1;
            sumS1 += meanDiff[j]*meanDiff[j];
        }

        double sum;
        double[] currentMeanDiff2;
        for (int i = 0; i < distances.length; i++) {
            // Calculate the mean and stddev
            sum = 0.0;
            currentMeanDiff2 = meanDiff2[i];
            for (int j = 0; j < vector.length; j++) {
                sum += meanDiff[j]*currentMeanDiff2[j];
            }

            distances[i] =  -(sum / Math.sqrt(sumS1 * sumS2[i]));
        }
        return distances;
    }

}
