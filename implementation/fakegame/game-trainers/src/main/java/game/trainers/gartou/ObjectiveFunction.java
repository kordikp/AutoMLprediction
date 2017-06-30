package game.trainers.gartou;

/**
 * <p>Title: Gartou Library</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Anicka Kucerova, java version Jan Drchal
 * @version 1.0
 */

/**
 * This class implements the so-called objective or fitness function.
 * This is the function which is going to be optimized.
 */
public interface ObjectiveFunction {
    /**
     * The dimension of optimized function.
     */
    public int getDim(); // public?

    /**
     * Defines the domain of optimized function.
     *
     * @param x
     * @param y
     */
    public double getDomain(int x, int y);

    public double getOptimum(); // !!!!!!!!!!!!!!!!!

    public double getPrecision();

    public boolean getReturnToDomain();

//  ObjectiveFunction[] F;


    /**
     * This method represents the objective function <i>f: <b>X</b>->Y</i>.
     *
     * @param oCH vector <i><b>X</b><i>
     * @return the value of <i>Y</i>
     */
    public double value(double[] oCH);

    /**
     * Evaluates the objective function <i>f: <b>X</b>->Y</i>.
     *
     * @param oCH vector <i><b>X</b><i>
     */
    public void evaluate(double[] oCH);
}