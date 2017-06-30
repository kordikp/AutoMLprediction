package game.models.ensemble;

import game.models.Model;
import game.models.ModelLearnable;

/**
 * Interface for all ensemble implementations
 * Author: cernyjn
 */
public interface ModelEnsemble extends ModelLearnable {


    /**
     * Learns all ensembled models even those models that are already learned.
     */
    public void relearn();

    /**
     * Relearns model at index modelIndex.
     *
     * @param modelIndex
     */
    public void learn(int modelIndex);

    /**
     * Returns the model identified by index.
     *
     * @param modelIndex index of the model
     */
    public Model getModel(int modelIndex);

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
     * Appends a model to to the ensemble.
     *
     * @param model Model to append
     */
    public void appendModel(Model model);

    /**
     * Remove model at position specified by index.
     *
     * @param modelIndex
     */
    public void removeModel(int modelIndex);

    /**
     * Number of models
     *
     * @return number of models
     */
    public int getModelsNumber();

}
