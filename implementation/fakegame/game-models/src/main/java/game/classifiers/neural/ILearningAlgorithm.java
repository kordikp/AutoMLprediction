/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.util.ArrayList;

/**
 * @author Administrator
 */
public interface ILearningAlgorithm {
    public void train(TrainingSet trainingSet, SlopeCalcParams partialDerivativeInfo, ISlopeCalcFunction partialDerivativeFunction) throws Exception;

    public void modifyWeights(TrainMode mode, ArrayList<Synapse> synapsesToModify) throws Exception;

    public String getType();
}
