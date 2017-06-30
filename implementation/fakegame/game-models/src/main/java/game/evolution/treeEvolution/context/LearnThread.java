package game.evolution.treeEvolution.context;

import game.evolution.treeEvolution.FitnessNode;
import game.evolution.treeEvolution.TreeNode;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Semaphore;

/**
 * Simple class to learn a classifier
 */
class LearnThread extends Thread {

    private FitnessContextBase context;
    private Object node;
    private double[] fitness;
    private int threadIndex;
    private Semaphore activeThreads;
    private boolean useValidSetOnLearn;

    public LearnThread(int threadIndex, FitnessContextBase context, Object node, double[] fitness, Semaphore activeThreads, boolean useValidSetOnLearn) {
        this.threadIndex = threadIndex;
        this.context = context;
        this.node = node;
        this.fitness = fitness;
        this.activeThreads = activeThreads;
        this.useValidSetOnLearn = useValidSetOnLearn;
    }

    public void run() {
        long threadId = Thread.currentThread().getId();
        ThreadMXBean threadManagement = ManagementFactory.getThreadMXBean();
        try {
            if (useValidSetOnLearn) {
                if (node instanceof TreeNode)
                    fitness[threadIndex] = context.getNonCachedTestFitness(((TreeNode) node).node);
                else if (node instanceof FitnessNode)
                    fitness[threadIndex] = context.getNonCachedTestFitness((FitnessNode) node);
            } else if (node instanceof TreeNode) {
                fitness[threadIndex] = context.getFitness((TreeNode) node);
            } else if (node instanceof FitnessNode) {
                fitness[threadIndex] = context.getNonCachedValidFitness((FitnessNode) node);
            } else {
                Logger log = Logger.getLogger(this.getClass());
                log.warn("unsupported class: " + node.getClass());
            }
        } catch (ThreadDeath e) { //catch thread termination
            Logger log = Logger.getLogger(this.getClass());
            log.warn("terminating thread [" + threadId + "] (" + (threadManagement.getThreadCpuTime(threadId) / 1000000000) + "s) " + node.toString());
        }
        activeThreads.release();
        if (context.getElapsedTime() != null)
            context.getElapsedTime().addToElapsedTimeMs(threadManagement.getThreadCpuTime(threadId) / 1000000);
    }
}
