

package game.classifiers.neural;

import java.util.ArrayList;

/**
 * This class represents any pattern, by pattern is meant any set of value
 *
 * @author Do Minh Duc
 */
public class Pattern {
    private ArrayList<Double> pattern;

    /**
     * Constructor
     *
     * @param pattern array with inputs
     */
    public Pattern(double[] pattern) {
        this.pattern = new ArrayList<Double>();
        for (int i = 0; i < pattern.length; i++) {
            this.pattern.add((Double) pattern[i]);
        }
    }

    /**
     * Constructor
     */
    public Pattern() {
        this.pattern = new ArrayList<Double>();
    }

    /**
     * Returns array with inputs value
     *
     * @return array with inputs value
     */
    public Double[] getPattern() {
        Double[] patternArray = new Double[this.size()];
        this.pattern.toArray(patternArray);
        return patternArray;
    }

    public ArrayList getPatternList() {
        return this.pattern;
    }

    /**
     * Adds input to pattern
     *
     * @param input input to be added
     */
    public void add(double input) {
        this.pattern.add((Double) input);
    }

    /**
     * Returns number of inputs in pattern
     *
     * @return number of inputs in pattern
     */
    public int size() {
        return pattern.size();
    }

    /**
     * Returns input at specific position
     *
     * @param index position of input to be returned
     * @return input value at position
     * @throws java.lang.Exception
     */
    public double get(int index) throws Exception {
        if (index < 0 || index >= this.size()) throw new Exception("InputPattern: get: index out of range");
        else return this.pattern.get(index);
    }

    /**
     * Sets input value at specific position
     *
     * @param index position of input to be modified
     * @param value new value of input
     * @throws java.lang.Exception
     */
    public void set(int index, double value) throws Exception {
        if (index < 0 || index >= this.size()) throw new Exception("InputPattern: set: input index out of range");
        else this.pattern.set(index, value);
    }

}
