package game.tools.distance;


public class DynamicTimeWarpingDistance extends DistanceMeasure {

    private double[][] dP2P;
    private double[][] D;

    public DynamicTimeWarpingDistance(double[][] vectors) {
        super(vectors);

        int dimensions = vectors[0].length;
        dP2P = new double[dimensions][dimensions];
        D = new double[dimensions][dimensions];
    }

    @Override
    public double[] getDistanceToAll(double[] vector) {
        double[] distances = new double[vectors.length];

        for (int i = 0; i < distances.length; i++) {
            distances[i] = calculateDistance(vector,vectors[i]);
        }
        return distances;
    }

    private double calculateDistance(double[] value1, double[] value2) {
		int i, j;
		// Build a point-to-point distance matrix
        double diff;
		for (i = 0; i < value1.length; i++) {
			for (j = 0; j < value2.length; j++) {
                diff = value1[i] - value2[j];
				dP2P[i][j] = diff*diff;
			}
		}
		// Check for some special cases due to ultra short time series
		if (value1.length == 1 && value2.length == 1) {
			return (Math.sqrt(dP2P[0][0]));
		}
		// Build the optimal distance matrix using a dynamic programming approach
		D[0][0] = dP2P[0][0]; // Starting point
		for (i = 1; i < value1.length; i++) { // Fill the first column of our
			// distance matrix with optimal
			// values
			D[i][0] = dP2P[i][0] + D[i - 1][0];
		}
		if (value2.length == 1) { // TS2 is a point
			double sum = 0;
			for (i = 0; i < value1.length; i++) {
				sum += D[i][0];
			}
			return (Math.sqrt(sum) / value1.length);
		}
		for (j = 1; j < value2.length; j++) { // Fill the first row of our
			// distance matrix with optimal
			// values
			D[0][j] = dP2P[0][j] + D[0][j - 1];
		}
		if (value1.length == 1) { // TS1 is a point
			double sum = 0;
			for (j = 0; j < value2.length; j++) {
				sum += D[0][j];
			}
			return (Math.sqrt(sum) / value2.length);
		}

        double min;
        int nextIStep;
        double step0;
        double step1;
        double step2;
		for (i = 1; i < value1.length; i++) { // Fill the rest
            nextIStep = i-1;
			for (j = 1; j < value2.length; j++) {
                min = D[nextIStep][j-1]; //step0
                step1 = D[nextIStep][j];
                step2 = D[i][j-1];
                //instead of Math.min
                if (step1 < min) min = step1;
                if (step2 < min) min = step2;

				D[i][j] = dP2P[i][j] + min;
			}
		}
		// Calculate the distance between the two time series through optimal alignment.
		i = value1.length - 1;
		j = value2.length - 1;
		int k = 1;
		double dist = D[i][j];
		while (i + j > 2) {
			if (i == 0) {
				j--;
			} else if (j == 0) {
				i--;
			} else {
                step0 = D[i - 1][j - 1];
                step1 = D[i - 1][j];
                step2 = D[i][j - 1];
				//instead of Math.min
                min = step0;
                if (step1 < min) min = step1;
                if (step2 < min) min = step2;

				if (min == step0) {
					i--;
					j--;
				} else if (min == step1) {
					i--;
				} else { //min == step2
					j--;
				}
			}
			k++;
			dist += D[i][j];
		}
		return (Math.sqrt(dist) / k);
	}

}
