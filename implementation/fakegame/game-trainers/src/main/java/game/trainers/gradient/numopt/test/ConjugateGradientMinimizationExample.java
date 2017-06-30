package game.trainers.gradient.numopt.test;

import game.trainers.gradient.conjugateGradient.ConjugateGradientMinimization;
import game.trainers.gradient.conjugateGradient.ConjugateGradientMinimizationException;
import game.trainers.gradient.numopt.LineSearchException;
import game.trainers.gradient.numopt.events.IterationAdapter;
import game.trainers.gradient.numopt.events.IterationEvent;
import game.trainers.gradient.numopt.events.OptimizationEvent;
import game.trainers.gradient.numopt.events.OptimizationListener;

import common.function.BasicObjectiveFunction;

/**
 * User: honza
 * Date: 17.2.2007
 * Time: 22:02:36
 */
public class ConjugateGradientMinimizationExample {

    public static void main(String[] args) {
        final BasicObjectiveFunction function = new TestFunction2();
        ConjugateGradientMinimization ex = new ConjugateGradientMinimization(function);

        ex.addOptimizationListener(new OptimizationListener() {

            public void OptimizationStart(OptimizationEvent oie) {

            }

            public void OptimizationEnd(OptimizationEvent oie) {
            }
        });

        ex.addIterationListener(new IterationAdapter() {

            public void IterationStart(IterationEvent oie) {

            }

            public void IterationEnd(IterationEvent oie) {
                ConjugateGradientMinimization cgm = (ConjugateGradientMinimization) oie.getSource();
            }
        });

        double[] initPs = {2.0, 3.0};

        try {
            ex.minimize(initPs);
        } catch (ConjugateGradientMinimizationException e) {
            e.printStackTrace();
        } catch (LineSearchException e) {
            e.printStackTrace();
        }
//        x=1/3, y=-1
//        NumericalDifferentiation.checkAnalyticGradient(function, new double[]{1.56, 1.1});
    }
}
