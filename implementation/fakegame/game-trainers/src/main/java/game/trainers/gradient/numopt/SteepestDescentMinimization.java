package game.trainers.gradient.numopt;

import common.MachineAccuracy;
import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 31.3.2007
 * Time: 20:22:15
 */
public class SteepestDescentMinimization extends MinimizationMethod {
    private double maxIterations;

    private final ObjectiveFunction func;
    private final int n;

    private StopCondition stopCondition;

    private LineSearch lineSearch;

    public SteepestDescentMinimization(ObjectiveFunction ofunc) {
        this(ofunc, LineSearchFactory.createDefault(ofunc));
    }

    private SteepestDescentMinimization(ObjectiveFunction ofunc, LineSearch olineSearch) {
        this(ofunc, olineSearch, MachineAccuracy.SQRT_EPSILON, 20000);
    }

    public SteepestDescentMinimization(ObjectiveFunction ofunc, LineSearch olineSearch, final double otolerance, final int omaxIterations) {
        func = ofunc;
        n = ofunc.getNumArguments();

        lineSearch = olineSearch;
        maxIterations = omaxIterations;

//        stopCondition = new SimpleStopCondition(func, otolerance);
        stopCondition = new PALStopCondition(otolerance);
    }

    public double getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(double maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void minimize(double[] ox) throws SteepestDescentMinimizationException, LineSearchException {
        fireOptimizationStart();

        double[] g = new double[n]; //gradient vector
        double[] p = new double[n]; //search direction
        double[] pold = new double[n]; //old search direction

        // set to initial point x0
        x = ox;

        // compute functional value and gradient in x0
        // if possible use analytic gradient otherwise numerical...
        if (func.isAnalyticGradient()) {
            //evaluates function + computes analytic gradient
            fx = func.evaluate(x, g);
        } else {
            //evaluates function + computes numerical gradient
            fx = func.evaluate(x);
            NumericalDifferentiation.gradientCD(func, x, g);
        }

        //initialize stop condition
        //stopCondition.init(fx);
        stopCondition.init(fx, x);

        //iterate until solution found or maxIterations exceeded
        for (iteration = 1; iteration <= maxIterations; iteration++) {
//            System.out.println("iteration = " + iteration + " f(x) = " + fx);
            fireIterationStart();

            //steepest descent direction
            for (int i = 0; i < n; i++) {
                p[i] = -g[i];
            }

            //BRENT Line search
            fx = lineSearch.minimize(x, p, fx, g);

            //test for convergence
//            if (stopCondition.stop(fx)) {
            if (stopCondition.stop(fx, x)) {
//                System.out.println("Accurate solution found.");
//                System.out.println("fx = " + fx);
                fireOptimizationEnd();
                return;
            }


            fireIterationEnd();
        }
        throw new SteepestDescentMinimizationException("Too many iterations.");
    }
}
