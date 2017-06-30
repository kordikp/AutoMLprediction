/*
 * APAIndividual.java
 * - represents one individuum for DifferentialEvolutionTrainer algorithm
 * Created on 21. June 2006, 14:47
 */

package game.trainers.gradient.DifferentialEvolution;

/**
 * @author Miroslav Janosik (janosm2@fel.cvut.cz)
 */
public class Individual {

    /* instantion variables */
    private double val[];       // vector of double values
    private double costValue;   // cost value

    /**
     * Creates a new instance of Individual
     *
     * @param size
     */
    public Individual(int size) {
        this.val = new double[size];
    }

    /**
     * returns vector of values
     */
    public double[] getValues() {
        return this.val;
    }

    /**
     * sets the cost value
     *
     * @param costValue
     */
    public void setCostValue(double costValue) {
        this.costValue = costValue;
    }

    /**
     * gets the cost value
     */
    public double getCostValue() {
        return this.costValue;
    }

    /**
     * sets one one value at index in vector
     *
     * @param index
     * @param value
     */
    public void setValueAt(int index, double value) {
        this.val[index] = value;
    }

    /**
     * gets one value at index in vector
     *
     * @param index
     */
    public double getValueAt(int index) {
        return this.val[index];
    }

    /**
     * returns new instance of individual: x = "this" + ind
     *
     * @param ind
     */
    public Individual plus(Individual ind) {
        Individual result = new Individual(val.length);
        for (int i = 0; i < val.length; i++) {
            double foo = this.val[i] + ind.getValueAt(i);
            result.setValueAt(i, foo);
        }
        return result;
    }

    /**
     * returns new instance of individual: x = "this" - ind
     *
     * @param ind
     */
    public Individual minus(Individual ind) {
        Individual result = new Individual(val.length);
        for (int i = 0; i < val.length; i++) {
            double foo = this.val[i] - ind.getValueAt(i);
            result.setValueAt(i, foo);
        }
        return result;
    }

    /**
     * returns new instance of individual: x = "this" * scalar
     *
     * @param scalar
     */
    public Individual timesScalar(double scalar) {
        Individual result = new Individual(val.length);
        for (int i = 0; i < val.length; i++) {
            double foo = this.val[i] * scalar;
            result.setValueAt(i, foo);
        }
        return result;
    }

    // fills vector with random values between min and max
    public void setRandomValues(double min, double max) {
        for (int i = 0; i < val.length; i++) {
            // x = (rnd(0,1) * abs(max - min)) + min
            this.val[i] = ((Math.random()) * (Math.abs(max - min))) + min;
        }
    }

    /**
     * standard toString() method
     */
    public String toString() {
        String s = "cost_value: " + this.costValue + "\tvector: [\t";
        for (double aVal : val) {
            s += aVal + ",\t";
        }
        return s + "]";
    }
}
