package game.trainers.gradient.conjugateGradient;

import game.trainers.gradient.numopt.LineSearch;
import game.trainers.gradient.numopt.LineSearchException;
import game.trainers.gradient.numopt.LineSearchFactory;
import game.trainers.gradient.numopt.MinimizationMethod;
import game.trainers.gradient.numopt.PALStopCondition;
import game.trainers.gradient.numopt.StopCondition;

import common.MachineAccuracy;
import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;

/**
 * Created by IntelliJ IDEA.
 * User: honza
 * Date: 17.2.2007
 * Time: 19:40:36
 * Method types are described in:
 * Nocedal, J. and Wright, S. J. (1999) Numerical Optimization. Springer.
 */
public class ConjugateGradientMinimization extends MinimizationMethod {
    public enum Method {
        FLETCHER_REEVES,
        POLAK_RIBIERE,
        BEALE_SORENSON_HESTENES_STIEFEL
    }

    private double maxIterations;
    private boolean usePlusMethod;


    private final ObjectiveFunction func;
    private final int n;

    private StopCondition stopCondition;

    private LineSearch lineSearch;

    private Method method;

    public ConjugateGradientMinimization(ObjectiveFunction ofunc) {
        this(ofunc, LineSearchFactory.createDefault(ofunc));
    }

    private ConjugateGradientMinimization(ObjectiveFunction ofunc, LineSearch olineSearch) {
        this(ofunc, olineSearch, MachineAccuracy.SQRT_EPSILON, Method.BEALE_SORENSON_HESTENES_STIEFEL, 20000);
    }

    public ConjugateGradientMinimization(ObjectiveFunction ofunc, LineSearch olineSearch, final double otolerance, final Method omethod, final int omaxIterations) {
        this(ofunc, olineSearch, otolerance, omethod, omaxIterations, true);
    }

    private ConjugateGradientMinimization(ObjectiveFunction ofunc, LineSearch olineSearch, final double otolerance, final Method omethod, final int omaxIterations, final boolean ousePlusMethod) {
        func = ofunc;
        n = ofunc.getNumArguments();
        lineSearch = olineSearch;
        method = omethod;
        maxIterations = omaxIterations;
        usePlusMethod = ousePlusMethod;

//        stopCondition = new SimpleStopCondition(func, otolerance);
        stopCondition = new PALStopCondition(otolerance);
    }

    public double getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(double maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void minimize(double[] ox0) throws ConjugateGradientMinimizationException, LineSearchException {
        fireOptimizationStart();

        double[] g = new double[n]; //gradient vector
        double[] gold = new double[n]; //previous gradient vector
        double[] p = new double[n]; //conjugate search direction
        double[] pold = new double[n]; //old conjugate direction
        double beta; //see [1]
        double gg, dgg; //temporary for computation of a new direction
        double slope;
        double orthoTest; //ortogonality test

        // set to initial point x0
        x = ox0;

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

        //set p0 to steepest descent direction p0 = -g0
        for (int i = 0; i < n; i++) {
            p[i] = -g[i];
        }

        //iterate until solution found or maxIterations exceeded
        for (iteration = 1; iteration <= maxIterations; iteration++) {
//            System.out.println("iteration = " + iteration);
            fireIterationStart();

            //save old gradient
            System.arraycopy(g, 0, gold, 0, n);

            try {
                //perform line search
                fx = lineSearch.minimize(x, p, fx, g);
            } catch (Exception e) {
                System.err.println("Unresolved exception catched !");
                System.err.println("The game.data cen be vizualized wrongly");
                System.err.println(e.toString());
            }

            //test for convergence
//            if (stopCondition.stop(fx)) {
            if (stopCondition.stop(fx, x)) {
//                System.out.println("Accurate solution found.");
//                System.out.println("fx = " + fx);
                fireOptimizationEnd();
                return;
            }

            //now compute beta for given method
            gg = 0.0;
            dgg = 0.0;
            for (int i = 0; i < n; i++) {
                switch (method) {
                    case BEALE_SORENSON_HESTENES_STIEFEL:
                        dgg += g[i] * (g[i] - gold[i]);
                        gg += p[i] * (g[i] - gold[i]);
                        break;
                    case POLAK_RIBIERE:
                        dgg += g[i] * (g[i] - gold[i]);
                        gg += gold[i] * gold[i];
                        break;
                    case FLETCHER_REEVES:
                        dgg += g[i] * g[i];
                        gg += gold[i] * gold[i];
                        break;
                }
            }

            //gradient ortogonality test
            orthoTest = Math.abs(dotProduct(g, gold)) / dotProduct(g, g);

            if (orthoTest >= 0.1 && iteration > 1) {
//                beta = 0.0;
                beta = dgg / gg;
//                System.out.println("orthoTest = " + orthoTest);
            } else {
                beta = dgg / gg; //now we have beta
            }

            if (usePlusMethod && beta < 0) {
//                Nocedal, J. and Wright, S. J. (1999) Numerical Optimization. Springer.
//                TODO check - it's not clear if it is appropriate to all methods
//                they mention only Polak-Ribiere
//                System.out.println("beta = 0");
                beta = 0.0;
            }

            //compute a new conjugate direction p(k+1) = -g(k+1) + beta(k+1)*p(k)
            System.arraycopy(p, 0, pold, 0, n); //but save the previous
            //TODO faster copy
            for (int i = 0; i < n; i++) {
                p[i] = -g[i] + beta * pold[i];
            }

            //compute slope
            slope = dotProduct(p, g);
            if (slope >= 0.0) {//if the slope is positive reset to the steepest descent direction
                System.out.println("slope >= 0: implement!!");
//                for (int i = 0; i < n; i++) {
//                    p[i] = -g[i];
//                }
            }

            fireIterationEnd();
        }
        throw new ConjugateGradientMinimizationException("Too many iterations.");
    }

    public static double dotProduct(double[] ovx, double[] ovy) {
        double ddot = 0.0;

        if (ovx.length <= 0) {
            return ddot;
        }

        for (int i = 0; i < ovx.length; i++) {
            ddot += ovx[i] * ovy[i];
        }
        return ddot;
    }


}
