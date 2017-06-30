package game.evolution.treeEvolution.evolutionControl;


import configuration.classifiers.ConnectableClassifierConfig;
import configuration.models.ConnectableModelConfig;
import game.data.AbstractGameData;
import game.data.MiningType;
import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.InnerFitnessNode;

import java.lang.management.ManagementFactory;

public class EvolutionUtils {


    /**
     * @param secondsDuration Total expected duration of computation.
     * @param data            Data that are computed. It is used to measure task complexity by using data instance number and inputs number.
     * @return Returns maximum time allowed for computation of one individual based on given data complexity and maximum allowed computation time.
     */
    public static long getMaxComputationTimeMs(long secondsDuration, AbstractGameData data) {
        double instanceCoef = Math.sqrt((double) data.getInstanceNumber() / 500);
        double inputsCoef = Math.sqrt((double) data.getINumber() / 10);
        long computationTimeS = (long) ((Math.sqrt(secondsDuration)) * (instanceCoef + inputsCoef));

        if (computationTimeS > secondsDuration / 10) computationTimeS = secondsDuration / 10;
        return computationTimeS * 1000;
    }

    /**
     * @return Returns number of seconds of CPU time of current JVM.
     */
    public static long getTime() {
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime() / 1000000000;
    }

    /**
     * Checks the input array and adds inputOptimizer to the top of the tree of each node. If there already is
     * inputOptimizer it is replaced if the number of its inputs does not match number of inputs of current data.
     *
     * @param generation Input array of nodes.
     */
    public static void addInputOptimizer(FitnessNode[] generation, AbstractGameData data) {
        for (int i = 0; i < generation.length; i++) {
            if (generation[i] instanceof ConnectableClassifierConfig) {
                ConnectableClassifierConfig cfg = (ConnectableClassifierConfig) generation[i];

                if (cfg.getSelectedInputs().length == data.getINumber()) continue;
                else generation[i] = cfg.getNode(0);
            }

            if (generation[i] instanceof ConnectableModelConfig) {
                ConnectableModelConfig cfg = (ConnectableModelConfig) generation[i];

                if (cfg.getSelectedInputs().length == data.getINumber()) continue;
                else generation[i] = cfg.getNode(0);
            }

            InnerFitnessNode inputOptimizer = createInputOptimizer(data);
            inputOptimizer.addNode(generation[i]);
            generation[i] = inputOptimizer;
        }
    }

    /**
     * @param data Data that configuration will be used on.
     * @return Returns config object that is responsible for optimizing inputs.
     */
    public static InnerFitnessNode createInputOptimizer(AbstractGameData data) {
        InnerFitnessNode operator = null;
        if (data.getDataType() == MiningType.CLASSIFICATION)
            operator = new ConnectableClassifierConfig(data.getINumber());
        else if (data.getDataType() == MiningType.REGRESSION) operator = new ConnectableModelConfig(data.getINumber());
        return operator;
    }

}
