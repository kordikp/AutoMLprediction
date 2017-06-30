package game.trainers.gradient.numopt;

import common.MachineAccuracy;
import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 31.3.2007
 * Time: 18:35:25
 */
public class SteepestDescentFixedStepMinimization extends MinimizationMethod {
    private double maxIterations;

    private final ObjectiveFunction func;
    private final int n;

    private StopCondition stopCondition;

    private double stepSize = 0.00001;
    private double momentum = 0.9;

    public SteepestDescentFixedStepMinimization(ObjectiveFunction ofunc) {
        this(ofunc, MachineAccuracy.SQRT_EPSILON, 20000);
    }

    public SteepestDescentFixedStepMinimization(ObjectiveFunction ofunc, final double otolerance, final int omaxIterations) {
        func = ofunc;
        n = ofunc.getNumArguments();
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

    public void minimize(double[] ox) throws SteepestDescentFixedStepMinimizationException {
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

            //steepest descent fixed step  with momentum
            for (int i = 0; i < n; i++) {
                p[i] = -stepSize * g[i] + momentum * pold[i];
                pold[i] = p[i];
            }

            for (int i = 0; i < n; i++) {
                x[i] += p[i];
            }

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
        throw new SteepestDescentFixedStepMinimizationException("Too many iterations.");
    }
}
