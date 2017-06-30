package game.trainers.gradient.numopt;

import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 5.5.2007
 * Time: 15:55:58
 */
public class LineSearchFactory {
    public static LineSearch createDefault(ObjectiveFunction ofunc) {
        LineSearch ls = new LineSearchStrongWolfe(ofunc);
//        LineSearch ls = new LineSearchBrentWithDerivatives(ofunc);
//        LineSearch ls = new LineSearchBrentNoDerivatives(ofunc);
//        lineSearchStrongWolfe.setFunctionTolerance(100 * MachineAccuracy.EPSILON);
        return ls;
    }
}
