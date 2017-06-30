package game.tools.distance;


public class JaccardSimilarity extends DistanceMeasure {

    private double[] wy;

    public JaccardSimilarity(double[][] vectors) {
        super(vectors);

        int dimensions = vectors[0].length;
        wy = new double[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < dimensions; j++) {
                wy[i] += vectors[i][j];
            }
        }
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double wx=0;
        for (int j = 0; j < vector.length; j++) {
            wx += vector[j];
        }

        double wxy;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            wxy = 0.0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                wxy += vector[j]*currentVector[j];
            }
            distances[i] = -(wxy / (wx + wy[i] - wxy));
        }
        return distances;
    }

}
