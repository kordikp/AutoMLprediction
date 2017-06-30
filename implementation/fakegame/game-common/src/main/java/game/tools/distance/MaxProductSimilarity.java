package game.tools.distance;


public class MaxProductSimilarity extends DistanceMeasure {

    public MaxProductSimilarity(double[][] vectors) {
        super(vectors);
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        double distance;
        double max;
        double[] currentVector;
        for (int i = 0; i < distances.length; i++) {
            max = Double.NEGATIVE_INFINITY;
            currentVector = vectors[i];
            for (int j = 0; j < vector.length; j++) {
                distance = vector[j] * currentVector[j];
                if (distance > max) max = distance;
            }
            //distance is undefined for values smaller or equal to 0
            if (max > 0.0) distances[i] = -max;
            else distances[i] = Double.POSITIVE_INFINITY;
        }
        return distances;
    }

}
