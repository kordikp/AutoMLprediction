package game.classifiers.ensemble;

import configuration.classifiers.ensemble.ClassifierCascadeGenProbConfig;
import game.classifiers.Classifier;

/**
 * Modification of stacking where metadata are class probabilities instead of output attributes
 * Author: cernyjn
 */
public class ClassifierCascadeGenProb extends ClassifierCascadeGen {

    /**
     * Creates new array, duplicates everything from previous array and adds new columns coresponding to output probabilities
     * of last learned model identified by pos.
     *
     * @param data 2D array of so far obtained data
     * @param pos  position of last learned model
     */
    protected double[][] addToData(double[][] data, int pos) {
        Classifier cls = ensClassifiers.get(pos);
        double[][] newModData = new double[learning_vectors][data[0].length + outputs];
        for (int i = 0; i < learning_vectors; i++) {
            System.arraycopy(data[i], 0, newModData[i], 0, data[i].length);
            //copy output probabilities from model cls to the empty end of metada
            System.arraycopy(cls.getOutputProbabilities(data[i]), 0, newModData[i], data[i].length, outputs);
        }
        return newModData;
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (!learned) learn();
        double[] modifiedInput = input_vector; // = new double[inputs+modelsNumber-1];

        double[] newModInput;
        for (int i = 0; i < numClassifiers - 1; i++) {
            newModInput = new double[modifiedInput.length + outputs];
            System.arraycopy(modifiedInput, 0, newModInput, 0, modifiedInput.length);
            //copy output probabilities of current classifier to the empty end of modified input array          
            System.arraycopy(ensClassifiers.get(i).getOutputProbabilities(modifiedInput), 0, newModInput, modifiedInput.length, outputs);
            modifiedInput = newModInput;
        }
        return ensClassifiers.get(numClassifiers - 1).getOutputProbabilities(modifiedInput);
    }

    /**
     * Last model has (global inputs + (numClassifiers-1)*outputs) inputs.
     * outputs = probability for each output.
     * Go over all of them and replace input markers from last to first.
     *
     * @return Array of equations for each output variable.
     */
    public String[] getEquations(String[] inputEquation) {
        return new String[0];
    }

    @Override
    public Class getConfigClass() {
        return ClassifierCascadeGenProbConfig.class;
    }
}
