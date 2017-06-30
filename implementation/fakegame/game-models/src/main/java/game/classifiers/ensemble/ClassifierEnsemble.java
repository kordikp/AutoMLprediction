package game.classifiers.ensemble;

import game.classifiers.Classifier;

/**
 * Interface for ensembling classifiers
 * Author: cernyjn
 */
public interface ClassifierEnsemble extends Classifier {

    /**
     * Learn model selected by modelIndex
     *
     * @param modelIndex
     */
    public void learn(int modelIndex);

    /**
     * Returns the classifier identified by index.
     *
     * @param ClassifierIndex index of the model
     */
    public Classifier getClassifier(int ClassifierIndex);

    /**
     * Sets classifier in given position specified by index.
     *
     * @param ClassifierIndex index of classifier (position in ensemble)
     * @param classifier
     */
    public void setClassifier(int ClassifierIndex, Classifier classifier);

    /**
     * Inserts classifier to position specified by index.
     *
     * @param ClassifierIndex position in ensemble
     * @param classifier
     */
    public void addClassifier(int ClassifierIndex, Classifier classifier);

    /**
     * Remove classifier at position specified by index.
     *
     * @param ClassifierIndex
     */
    public void removeClassifier(int ClassifierIndex);

    /**
     * Number of classifiers
     *
     * @return number of classifiers
     */
    public int getClasifiersNumber();
}
