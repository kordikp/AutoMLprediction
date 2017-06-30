package game.tools.distance;


public class InnerProductSimilarity extends DistanceMeasure {

    public InnerProductSimilarity(double[][] vectors) {
        super(vectors);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double sum;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            sum=0;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                sum += vector[j]*currentVector[j];
            }
            distances[i] = -sum;
        }
        return distances;
    }

}
