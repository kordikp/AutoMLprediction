package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierStackingProbConfig;
import game.classifiers.Classifier;
import game.utils.MyRandom;

import java.util.Vector;

/**
 * Modification of stacking where metadata are class probabilities instead of output attributes
 * Author: cernyjn
 */
public class ClassifierStackingProb extends ClassifierStacking {

    protected void learnMetamodel(Classifier metaModel) {
        double[] metaData = new double[(numClassifiers - 1) * outputs];
        MyRandom rndGenerator = new MyRandom(learning_vectors);
        int rnd;
        int min = Math.min(metaModel.getMaxLearningVectors(), learning_vectors);

        //gather metadata
        for (int i = 0; i < min; i++) {
            rnd = rndGenerator.getRandom(learning_vectors);
            for (int j = 0; j < numClassifiers - 1; j++) {
                System.arraycopy(ensClassifiers.get(j).getOutputProbabilities(inputVect[rnd]), 0, metaData, outputs * j, outputs);
            }
            metaModel.storeLearningVector(metaData, target[rnd]);
        }
        //learn last model from metadata
        metaModel.learn();
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] metaData = new double[(numClassifiers - 1) * outputs];
        for (int i = 0; i < numClassifiers - 1; i++) {
            System.arraycopy(ensClassifiers.get(i).getOutputProbabilities(input_vector), 0, metaData, outputs * i, outputs);
        }
        return ensClassifiers.get(numClassifiers - 1).getOutputProbabilities(metaData);
    }

    @Override
    public Class getConfigClass() {
        return ClassifierStackingProbConfig.class;
    }
}
