/**
 * @author Ondrej F?l?pek
 * @version 0.1
 */

package configuration.game.trainers;

import configuration.AbstractCfgBean;

/**
 * Config class for PALConjugateGradientTrainer
 */
public class PALConjugateGradientConfig extends AbstractCfgBean {
    private double tolfx, tolx;
    private int method, minValue, maxValue, maxIterations;
    //    private static String[] methods = {"Fletcher-Reeves", "Polak-Ribiere", "Hestenes-Stiefel"};

    /**
     * inicialises parametres to its default values
     */
    public PALConjugateGradientConfig() {
        tolfx = 0.5;
        tolx = 1;
        method = 1;
        minValue = -1000;
        maxValue = 1000;
        maxIterations = 1000;
    }

    /**
     * Get value of parameter tolfx
     *
     * @return value of tolfx
     */
    public double getTolfx() {
        return tolfx;
    }

    /**
     * Get value of parameter tolx
     *
     * @return value of tolx
     */
    public double getTolx() {
        return tolx;
    }

    /**
     * Get type of direction update method
     *
     * @return type of method
     */
    public int getMethod() {
        return method;
    }

    /**
     * Get minimum allowed value - lower bound of data
     *
     * @return minimum value of input
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Get maximum allowed value - upper bound of data
     *
     * @return maximum value of input
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Get type of direction update method
     *
     * @return maximum value of input
     */
    public int getMaxIterations() {
        return maxIterations;
    }


}
