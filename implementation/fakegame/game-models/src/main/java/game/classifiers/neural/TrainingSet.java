
package game.classifiers.neural;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents the whole training set
 *
 * @author Do Minh Duc
 */
public class TrainingSet implements Serializable {
    private ArrayList<TrainingPattern> trainingSet;

    /**
     * Constructor
     *
     * @param trainingSet arraylist of training patterns
     */
    public TrainingSet(ArrayList<TrainingPattern> trainingSet) {
        this.trainingSet = trainingSet;
    }

    /**
     * Constructor
     */
    public TrainingSet() {
        this.trainingSet = new ArrayList<TrainingPattern>();
    }

    /**
     * Adds new training pattern
     *
     * @param trainingPattern training pattern to be added
     */
    public void addTrainingPattern(TrainingPattern trainingPattern) {
        this.trainingSet.add(trainingPattern);
    }

    /**
     * Returns training set
     *
     * @return arraylist with all training patterns
     */
    public ArrayList<TrainingPattern> getTraningSet() {
        return this.trainingSet;
    }

    /**
     * Returns specific training pattern
     *
     * @param index position of training pattern
     * @return specific training pattern
     * @throws java.lang.Exception
     */
    public TrainingPattern getTrainingPattern(int index) throws Exception {
        if (index < 0 || index >= this.trainingSet.size())
            throw new Exception("TrainingSet: getTrainingPattern: index out of range");
        return this.trainingSet.get(index);
    }

    /**
     * Returns number of training patterns in set
     *
     * @return number of training patterns in set
     */
    public int size() {
        return trainingSet.size();
    }
}
