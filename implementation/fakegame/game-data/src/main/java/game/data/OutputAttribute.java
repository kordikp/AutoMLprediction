/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.data;

/**
 * class represents general output attribute
 */
public class OutputAttribute {
    private String name;
    private double min;
    private double max;
    double val;
    private int significance;
    private boolean positive;
    private final static int MIN_SIGN = 0;
    private final static int MAX_SIGN = 1000; // minimal and maximal significance of an output attribute

    /**
     * inicialises output
     *
     * @param iname
     * @param imax
     * @param imin
     * @param ipos
     * @param isign
     */
    public OutputAttribute(String iname, double imax, double imin, boolean ipos, int isign) {
        name = iname;
        max = imax;
        min = imin;
        positive = ipos;
        val = min;
        if (isign < MIN_SIGN) isign = MIN_SIGN;
        if (isign > MAX_SIGN) isign = MAX_SIGN;
        significance = isign;
    }

    public String getName() {
        return name;
    }

    /*returns range (max-min)*/
    public double getScrollCoef() {
        return (max - min);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getSign() {
        return significance;
    }

    public double getValue() {
        return val;
    }

    public double getValueFromMin() {
        return (val - min);
    }

    public boolean isPositive() {
        return positive;
    }

    /**
     * disabled
     */
    public double getStandardSign() {
        /*int ms = getMaxSignificance();
   return ((double)(significance - MIN_SIGN)/(double)(ms - MIN_SIGN));*/
        return 0;
    }

    /**
     * returns the value normalized in <0,1> range
     */
    public double getStandardValue() {
        return ((val - min) / (max - min));
    }

    /**
     * sets the value normalized in <0,1> range, returns true if succeeded
     *
     * @param stVal
     */
    public boolean setStandardValue(double stVal) {
        return setValue(stVal * (max - min) + min);
    }

    /**
     * returns the original value denormalized from <0,1> range
     *
     * @param stVal
     */
    public double decodeStandardValue(double stVal) {
        return (stVal * (max - min) + min);
    }

    /**
     * returns normalized value in range <0,1>
     *
     * @param value
     */
    public double getStandardValue(double value) {
        return ((value - min) / (max - min));
    }

    public boolean setValue(double mval) {
        //if(mval >= min)
        //     if(mval <= max)
        //     {
        val = mval;
        return true;
        //     }
        //  return false;
    }

    public void modifyValues(double imax, double imin, boolean ipos, int isign) {
        max = imax;
        min = imin;
        positive = ipos;
        val = min;
        significance = isign;
    }
}
