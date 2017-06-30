package common;

/**
 * User: honza
 * Date: Jun 1, 2007
 * Time: 9:52:25 AM
 */
public class MatrixUtil {
    //TODO min is always 0 for projection normalization
    public static double[][] normalize(double[][] omatrix) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double[] value : omatrix) {
            for (double aDouble : value) {
                if (min > aDouble) {
                    min = aDouble;
                }
                if (max < aDouble) {
                    max = aDouble;
                }
            }
        }
        for (int i = 0; i < omatrix.length; i++) {
            for (int j = 0; j < omatrix[i].length; j++) {
                omatrix[i][j] = (min + omatrix[i][j]) / (max - min);
            }
        }
        return omatrix;
    }

    public static double[][] squash(double[][] omatrix, double ooffset, double oalpha) {
        for (int i = 0; i < omatrix.length; i++) {
            for (int j = 0; j < omatrix[i].length; j++) {
                omatrix[i][j] = 1.0 / (1.0 + Math.exp(-oalpha * (omatrix[i][j] - ooffset)));
            }
        }
        return omatrix;
    }

    public static double[][] copyDeep(double[][] omatrix) {
        double[][] t = new double[omatrix.length][];
        for (int i = 0; i < omatrix.length; i++) {
            t[i] = omatrix[i].clone();
        }
        return t;
    }

    public static double[][] triangularToComplete(double[][] omatrix) {
        int n = omatrix.length + 1;
        double[][] M = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i < j) {
                    M[i][j] = omatrix[i][j - 1 - i];
                    M[j][i] = M[i][j];
                }
            }
        }
        return M;
    }
}
