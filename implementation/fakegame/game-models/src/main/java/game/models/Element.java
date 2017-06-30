/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.models;

/**
 * This class represents one element in polynomial transfer function (CombiNeuron)
 * <br> enabled 0 0 0 1 0 1 0 0
 * <br> index   2 1 0 3 2 1 1 0
 * <br> in this case the element has the following transfer function: a * x3^3 * x5
 */
public class Element implements java.io.Serializable, Cloneable {
    double a;
    int[] index;
    boolean[] enabled;
    int indexes;

    public Element(double coef, int[] ienabled, int[] idx, int number) {
        indexes = number;
        setCoefficient(coef);
        setEnabled(ienabled);
        setIndexes(idx);
    }

    public Element(Element e) {
        indexes = e.indexes;
        setCoefficient(e.getCoefficent());
        enabled = new boolean[indexes];
        System.arraycopy(e.enabled, 0, enabled, 0, indexes);
        setIndexes(e.index);
    }

    public double getCoefficent() {
        return a;
    }

    public void setCoefficient(double c) {
        a = c;
    }

    public int getIndex(int num) {
        if (enabled[num])
            return index[num];
        return -1;
    }

    public int getNum() {
        return indexes;
    }

    void setIndexes(int[] idx) {
        index = new int[indexes];
        System.arraycopy(idx, 0, index, 0, indexes);
    }

    public void setEnabled(int[] ienabled) {
        enabled = new boolean[indexes];
        for (int i = 0; i < indexes; i++) {
            enabled[i] = ienabled[i] == 1;
        }
    }

    int[] getEnabled() {
        int[] en = new int[indexes];
        for (int i = 0; i < indexes; i++) {
            if (enabled[i]) en[i] = 1;
            else en[i] = 0;
        }
        return en;
    }

    /**
     * setIndex modifies the specified index
     *
     * @param wh int the number of active input
     * @param in int how many times multiplying this input
     */
    public void setIndex(int wh, int in) {
        if (enabled[wh]) {
            index[wh] = in;
        }
    }

    public Element clone() {
        try {
            super.clone();
            return new Element(a, getEnabled(), index, indexes);
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();

        }


    }

    //returns false if all indexes are disabled
    public boolean isEnabled() {

        for (int i = 0; i < indexes; i++) {
            if (enabled[i] && index[i] > 0) return true;

        }

        return false;
    }
}
