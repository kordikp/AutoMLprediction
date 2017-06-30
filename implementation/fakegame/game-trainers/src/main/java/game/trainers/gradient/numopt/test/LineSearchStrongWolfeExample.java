package game.trainers.gradient.numopt.test;

import game.trainers.gradient.numopt.LineSearchException;
import game.trainers.gradient.numopt.LineSearchStrongWolfe;

import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 24.3.2007
 * Time: 20:23:25
 */
public class LineSearchStrongWolfeExample {
    public static void main(String[] args) {
        ObjectiveFunction func = new TestFunction1b();
        LineSearchStrongWolfe lmds = new LineSearchStrongWolfe(func);
        double[] arg = {1.0};
        double f = func.evaluate(arg);
        double[] grad = new double[func.getNumArguments()];
        func.gradient(arg, grad);
        double[] dir = {-grad[0]};

        double fAlpha = Double.NaN;
        try {
            fAlpha = lmds.minimize(arg, dir, f, grad);
        } catch (LineSearchException e) {
            e.printStackTrace();
        }
    }
}
