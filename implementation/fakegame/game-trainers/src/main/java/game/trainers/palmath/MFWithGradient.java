// MFWithGradient.java
//
// (c) 2000-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package game.trainers.palmath;


/**
 * interface for a function of several variables with a gradient
 *
 * @author Korbinian Strimmer
 * @version $Id: MFWithGradient.java,v 1.2 2007/05/01 15:25:31 kordikp Exp $
 */
public interface MFWithGradient extends MultivariateFunction {
    /**
     * compute both function value and gradient at a point
     *
     * @param argument function argument (vector)
     * @param gradient gradient (on return)
     * @return function value
     */
    double evaluate(double[] argument, double[] gradient);

    /**
     * compute gradient at a point
     *
     * @param argument function argument (vector)
     * @param gradient gradient (on return)
     */
    void computeGradient(double[] argument, double[] gradient);
}
