package game.classifiers;

import game.models.Model;

/**
 * Interface for classifiers
 * Author: cernyjn
 */
public interface ClassifierSingle extends Classifier {
    /**
     * Returns the model identified by index.
     *
     * @param modelIndex index of the model
     */
    public Model getModel(int modelIndex);

    /**
     * Returns all models.
     */
    public Model[] getAllModels();

    /**
     * Sets model in given position specified by index.
     *
     * @param modelIndex index of model (position in ensemble)
     * @param model
     */
    public void setModel(int modelIndex, Model model);

    /**
     * Inserts model to position specified by modelIndex.
     *
     * @param modelIndex position in ensemble
     * @param model
     */
    public void addModel(int modelIndex, Model model);

    /**
     * Remove model at position specified by index.
     *
     * @param modelIndex
     */
    public void removeModel(int modelIndex);


}
