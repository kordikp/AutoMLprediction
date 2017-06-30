/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.data;


/**
 * class represents general input factors
 */
public class InputFactor implements OutputProducer {
    String name;
    private double min;
    private double max;
    private double med;
    private double val;
    private boolean cont;

    /**
     * inicialises input
     *
     * @param iname
     * @param imax
     * @param imin
     * @param imed
     * @param icont
     * @param color
     */
    public InputFactor(String iname, double imax, double imin, double imed, boolean icont) {
        name = iname;
        max = imax;
        min = imin;
        med = imed;
        val = min;
        cont = icont;

    }

    public void setMode(Mode mode) {
        //  allways passive mode - the getOutput() function is not further propagated
    }

    public Mode getMode() {
        return Mode.PASSIVE;
    }

    public double getOutput() {
        return val;
    }

    /**
     * name of the input
     */
    public String getName() {
        return name;
    }

    public String toEquation() {
        return name;
    }

    /**
     * range (max-min)
     */
    public double getScrollCoef() {
        return (max - min);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMed() {
        return med;
    }

    public boolean getCont() {
        return cont;
    }


    public double getValueFromMin() {
        return (val - min);
    }

    public double getValue() {
        return val;
    }

    /**
     * returns normalized value in range <0,1>
     */
    public double getStandardValue() {
        return ((val - min) / (max - min));
    }

    /**
     * returns normalized value in range <0,1>
     *
     * @param value
     */
    public double getStandardValue(double value) {
        return ((value - min) / (max - min));
    }

    /**
     * returns and sets the value normalized in <0,1> range
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

    public boolean setValue(double mval) {
        //   if(mval >= min)
        //      if(mval <= max)
        //      {
        val = mval;
        return true;
        //      }
        // return false;
    }

    public void modifyValues(double imax, double imin, double imed, boolean icont) {
        max = imax;
        min = imin;
        med = imed;
        val = min;
        cont = icont;
    }
}
