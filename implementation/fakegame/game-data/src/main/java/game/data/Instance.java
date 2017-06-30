/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.data;

/**
 * class represents instance (group's data)
 */
public class Instance {
    private String name;
    private double[] ifact;
    private double[] oattr;
    private double[] is;
    private double[] os;
    private int input, output;

    double criteriaFunction = 0;


    public Instance(String iname, double[] ivect, double[] ovect) {
        ifact = new double[ivect.length];
        oattr = new double[ovect.length];
        is = new double[ivect.length];
        os = new double[ovect.length];
        name = iname;
        setInputVect(ivect);
        setOutputVect(ovect);
    }

    public void refreshStdValues() {
        for (int i = 0; i < input; i++) this.computeStiValue(i);
        for (int i = 0; i < output; i++) this.computeStoValue(i);
    }

    void setInputVect(double[] ivect) {
        System.arraycopy(ivect, 0, ifact, 0, ivect.length);
    }

    void setOutputVect(double[] ovect) {
        System.arraycopy(ovect, 0, oattr, 0, ovect.length);
    }

    public String getName() {
        return name;
    }

    /**
     * returns instance value for input factor[num]
     *
     * @param num
     */
    public double getiVal(int num) {
        return (ifact[num]);
    }

    public void setiVal(int num, double val) {
        ifact[num] = val;
    }

    /**
     * returns instance value for output attribute[num]
     *
     * @param num
     */
    public double getoVal(int num) {
        return (oattr[num]);
    }

    public void setoVal(int num, double val) {
        oattr[num] = val;
    }

    /**
     * returns the output value normalized in <0,1> range
     *
     * @param num
     */
    public double getStoValue(int num) {
        return (os[num]);
    }

    /**
     * returns the input value normalized in <0,1> range
     *
     * @param num
     */
    public double getStiValue(int num) {
        return (is[num]);
    }

    /**
     * sets the output value normalized in <0,1> range
     *
     * @param num
     */
    void computeStoValue(int num) {
        //    os[num] = (oattr[num] - ((OutputAttribute)myD.oAttr.elementAt(num)).getMin()) / (((OutputAttribute)myD.oAttr.elementAt(num)).getMax() - ((OutputAttribute)myD.oAttr.elementAt(num)).getMin());
    }

    /**
     * sets the input value normalized in <0,1> range
     *
     * @param num
     */
    void computeStiValue(int num) {
        //   is[num] = (ifact[num] - ((InputFactor)myD.iFactor.elementAt(num)).getMin()) / (((InputFactor)myD.iFactor.elementAt(num)).getMax() - ((InputFactor)myD.iFactor.elementAt(num)).getMin());
    }

    /**
     * sets instance values to correspond to current data vector
     */
    public void modifyValues() {
        // for (int i = 0; i < myD.getINumber(); i++)
        //    ifact[i] = ((InputFactor)myD.iFactor.elementAt(i)).getValue();
        //for (int i = 0; i < myD.getONumber(); i++)
        //    oattr[i] = ((OutputAttribute)myD.oAttr.elementAt(i)).getValue();
    }

    /**
     * sets current data vector to correspond to instance values
     */
    public void setImplicitValues() {
        // for (int i = 0; i < input; i++)
        //    ((InputFactor)myD.iFactor.elementAt(i)).setValue(ifact[i]);
        // for (int i = 0; i < output; i++)
        //    ((OutputAttribute)myD.oAttr.elementAt(i)).setValue(oattr[i]);
    }
}
