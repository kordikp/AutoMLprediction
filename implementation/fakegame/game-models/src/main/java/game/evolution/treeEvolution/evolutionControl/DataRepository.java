package game.evolution.treeEvolution.evolutionControl;

import game.evolution.treeEvolution.HashTableContainer;

/**
 * Interface for saving/loading metada
 */
public interface DataRepository {
    public void saveData(int id, FitnessContainer[] data);

    public int saveMetaData(Object[] metaData);

    public void saveMetaData(int id, Object[] metaData);

    public String[][] loadMetaData();

    public FitnessContainer[] loadData(int id);

    public int updateMetaData(int oldId, Object[] newMetaData);
}
