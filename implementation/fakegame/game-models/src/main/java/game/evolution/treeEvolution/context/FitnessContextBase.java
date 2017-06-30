package game.evolution.treeEvolution.context;

import game.data.*;
import game.evolution.treeEvolution.*;
import game.evolution.treeEvolution.evolutionControl.ElapsedTime;
import game.utils.MyRandom;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for fitness config
 */
public abstract class FitnessContextBase implements FitnessContext {
    protected AbstractGameData data;
    protected Vector<OutputProducer> inp;
    protected MinMaxDataNormalizer norm;

    protected int dataNum;

    protected int[] learnIndex;
    protected int[] validIndex;
    protected int[] testIndex;
    protected int[] finalLearnIndex;

    protected int modelsBeforeCacheUse = 3;
    protected double testDataPercent = 0.2;
    protected double validDataPercent = 0.2;
    protected long maxComputationTimeMs = Long.MAX_VALUE;

    protected double bestFitness;
    protected int cacheUse;
    protected int modelsComputed;

    protected String bestConfig;
    protected double bestConfigFitness;
    protected FitnessNode bestModelConfig;

    protected Hashtable<String, HashTableContainer> cachedConfigs;

    protected int maxModels = Integer.MAX_VALUE;
    protected Logger log;

    //parallel processing
    protected Semaphore parallelLock;
    protected boolean parallelComputation = false;
    protected static int numberOfThreads = Runtime.getRuntime().availableProcessors() - 1;
    //computation time
    protected ElapsedTime elapsedTime;

    protected class Fitness {
        public double validFitness;
        public double testFitness;

        public Fitness(double validFitness, double testFitness) {
            this.validFitness = validFitness;
            this.testFitness = testFitness;
        }

        public Fitness() {
        }
    }

    /**
     * @param obj Config file.
     * @return Returns fitness of the given config file.
     */
    protected abstract Fitness getModelFitness(FitnessNode obj);

    /**
     * @param node Input configuration.
     * @return Returns fitness of the given config file.
     * Learns model on learn + valid data.
     */
    protected abstract Fitness getFitnessOnLearnValid(FitnessNode node);

    /**
     * @return Returns predefined config of the best model. Predefined config is configuration with fixed positions of
     * models (ie model is not generated randomly from this config).
     */
    public abstract FitnessNode getBestModelPredefinedConfig();

    public void init(AbstractGameData data) {
        initContextVariables(data);
        divideLearnData();
    }

    /**
     * Performs learning process, learning model on LEARN data. Does not use cache in any way.
     *
     * @param node Input node.
     * @return Returns VALID fitness.
     */
    public double getNonCachedValidFitness(FitnessNode node) {
        try {
            double fitness = getModelFitness(node).validFitness;
            log.debug("FITNESS: " + fitness);
            return fitness;
        } catch (OutOfMemoryError e) {
            log.debug("no enough memory");
            return Double.NEGATIVE_INFINITY;
        } catch (Exception e) {
            logLearningError(e, node.toString());
            return Double.NEGATIVE_INFINITY;
        }
    }

    /**
     * Performs learning process, learning model on LEARN + VALID data. Does not use cache in any way.
     *
     * @param node Input node.
     * @return Returns TEST fitness.
     */
    public double getNonCachedTestFitness(FitnessNode node) {
        try {
            double fitness = getFitnessOnLearnValid(node).testFitness;
            log.debug("FITNESS: " + fitness);
            return fitness;
        } catch (OutOfMemoryError e) {
            log.debug("no enough memory");
            return Double.NEGATIVE_INFINITY;
        } catch (Exception e) {
            logLearningError(e, node.toString());
            return Double.NEGATIVE_INFINITY;
        }
    }

    /**
     * Performs initialization of config, but respecting vector belonging to learnValid or test sets. LearnValid set is
     * then divided by context specific methods. Unspecified data in the indexes are divided by context specific methods,
     * trying to preserve given learn/valid/test sets ratios.
     *
     * @param data                 Input data, all indexes must be indexes in given data.
     * @param inputLearnValidIndex Indexes of instances belonging to validation and learning set.
     * @param inputTestIndex       Indexes of instances belonging to test set.
     */
    public void init(AbstractGameData data, int[] inputLearnValidIndex, int[] inputTestIndex) {
        initContextVariables(data);

        double oldTestDataPercent = testDataPercent;
        int[] learnIdx = new int[0];
        int[] validIdx = new int[0];
        if (inputLearnValidIndex.length > 0) {
            testDataPercent = 0;
            divideLearnData(inputLearnValidIndex);
            learnIdx = learnIndex;
            validIdx = validIndex;
        }

        //recompute test data percent to match exactly given ratio if input learn and test indexes does not have desired ratio
        int[] addedIndexes = getComplementOfSets(inputLearnValidIndex, inputTestIndex);
        testDataPercent = (oldTestDataPercent * data.getInstanceNumber() - inputTestIndex.length) / addedIndexes.length;
        divideLearnData(addedIndexes);
        testDataPercent = oldTestDataPercent;
        //merge data
        learnIndex = mergeArrays(learnIdx, learnIndex);
        validIndex = mergeArrays(validIdx, validIndex);
        testIndex = mergeArrays(inputTestIndex, testIndex);
        finalLearnIndex = mergeArrays(inputLearnValidIndex, finalLearnIndex);
    }

    protected void logLearningError(Exception e, String configToString) {
        StackTraceElement[] stack = e.getStackTrace();
        String errorMsg = "learning error " + e.toString();
        if (stack != null && stack.length > 0) errorMsg += " in " + stack[0].toString();
        log.warn(errorMsg + " in config " + configToString);
    }

    /**
     * @param inputLearnValidIndex Set of learn and valid indexes.
     * @param inputTestIndex       Set of test indexes.
     * @return Returns complement of input space with regard to context data (ie. indexes that are not in the 2 input sets).
     */
    protected int[] getComplementOfSets(int[] inputLearnValidIndex, int[] inputTestIndex) {
        boolean[] allIndexes = new boolean[data.getInstanceNumber()];
        for (int i = 0; i < inputLearnValidIndex.length; i++) allIndexes[inputLearnValidIndex[i]] = true;
        for (int i = 0; i < inputTestIndex.length; i++) allIndexes[inputTestIndex[i]] = true;

        int[] addedIndexes = new int[data.getInstanceNumber() - inputLearnValidIndex.length - inputTestIndex.length];
        int idx = 0;
        for (int i = 0; i < allIndexes.length; i++) {
            if (allIndexes[i] == false) addedIndexes[idx++] = i;
        }
        return addedIndexes;
    }

    /**
     * Performs initialization with given test data. Learn data are divided into learn and valid test according to context
     * specific methods.
     *
     * @param learnData Input learn data.
     * @param testData  Input test data.
     */
    public void init(AbstractGameData learnData, AbstractGameData testData) {
        double[][] inputLearn = learnData.getInputVectors();
        double[][] outputLearn = learnData.getOutputAttrs();

        double[][] inputTest = testData.getInputVectors();
        double[][] outputTest = testData.getOutputAttrs();

        double[][] inputData = mergeArrays(inputLearn, inputTest);
        double[][] outputData = mergeArrays(outputLearn, outputTest);

        data = new ArrayGameData(inputData, outputData);

        initContextVariables(data);

        int[] indexes = new int[inputLearn.length];
        for (int i = 0; i < indexes.length; i++) indexes[i] = i;

        testDataPercent = 0;
        divideLearnData(indexes);

        testIndex = new int[inputTest.length];
        for (int i = 0; i < testIndex.length; i++) {
            testIndex[i] = i + inputLearn.length;
        }
        testDataPercent = testIndex.length / data.getInstanceNumber();
    }

    protected int[] mergeArrays(int[] array1, int[] array2) {
        int[] finalArray = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, finalArray, 0, array1.length);
        System.arraycopy(array2, 0, finalArray, array1.length, array2.length);
        return finalArray;
    }

    protected double[][] mergeArrays(double[][] array1, double[][] array2) {
        double[][] finalArray = new double[array1.length + array2.length][array1[0].length];
        for (int i = 0; i < array1.length; i++) {
            System.arraycopy(array1[i], 0, finalArray[i], 0, array1[i].length);
        }

        for (int i = 0; i < array2.length; i++) {
            System.arraycopy(array2[i], 0, finalArray[i + array1.length], 0, array2[i].length);
        }
        return finalArray;
    }

    /**
     * Performs additional fitness computation on bestNode to verify it's fitness. Returns VALID fitness.
     */
    public double verifyBestNode() {
        return verifyFitness(getBestNode());
    }

    /**
     * @param obj Input TreeNode object.
     * @return Returns average VALID fitness of the individual fully verified by multiple computations. Number of theese
     * computations depends on context cache settings.
     */
    public double verifyFitness(TreeNode obj) {
        String key = obj.toString();
        int computedSoFar = 0;
        if (cachedConfigs.containsKey(key)) computedSoFar = cachedConfigs.get(key).occurrences;

        TreeNode[] nodesToCompute = new TreeNode[Math.max(0, modelsBeforeCacheUse - computedSoFar)];
        for (int i = 0; i < nodesToCompute.length; i++) nodesToCompute[i] = obj;

        log.debug("VERIFYING(" + computedSoFar + "/" + modelsBeforeCacheUse + "): " + key);
        getFitness(nodesToCompute);

        return getFitness(obj);
    }

    /**
     * @param node Input config file
     * @return Returns verified test fitness on final test data of the given node. Learns model on learn+valid data.
     */
    public double getFinalTestFitness(TreeNode node) {
        TreeNode[] nodesToCompute = new TreeNode[modelsBeforeCacheUse];
        for (int i = 0; i < nodesToCompute.length; i++) nodesToCompute[i] = node;

        log.debug("VERIFYING(" + modelsBeforeCacheUse + "x): " + node.toString());
        double[] fitness = getFitness(nodesToCompute, true);

        double avgFitness = 0;
        for (int i = 0; i < fitness.length; i++) avgFitness += fitness[i];

        return avgFitness / fitness.length;
    }

    protected void initContextVariables(AbstractGameData data) {
        this.data = data;
        data.refreshDataVectors();

        inp = data.getInputFeatures();

        norm = new MinMaxDataNormalizer();
        norm.init(data.getInputVectors(), data.getOutputAttrs());
        dataNum = data.getInstanceNumber();
        bestFitness = Double.NEGATIVE_INFINITY;
        bestConfigFitness = Double.NEGATIVE_INFINITY;
        log = Logger.getLogger(this.getClass());

        cachedConfigs = new Hashtable<String, HashTableContainer>(500);

        parallelLock = new Semaphore(1);
        printSettings();
    }

    public void printSettings() {
        log.info("-----------------------------------------------");
        log.info("CONTEXT CONFIGURATION:");
        log.info("-----------------------------------------------");
        log.info("Data size: " + data.getInstanceNumber() + "x" + data.getINumber());
        log.info("Type: " + getClass().getSimpleName());
        log.info("Cache use after computations: " + modelsBeforeCacheUse);
        log.info("Test data percent: " + testDataPercent);
        log.info("Valid data percent: " + validDataPercent);
        log.info("Number of threads: " + numberOfThreads);
        log.info("Max computation time for individual[s]:" + maxComputationTimeMs / 1000);
        log.info("-----------------------------------------------");
    }

    /**
     * @return Returns best config according to VALID fitness data.
     */
    protected String findBestConfig() {
        Enumeration<String> keys = cachedConfigs.keys();
        Enumeration<HashTableContainer> values = cachedConfigs.elements();

        double curValue;
        String curKey;
        double max = Double.NEGATIVE_INFINITY;
        String bestConfig = null;
        while (values.hasMoreElements()) {
            curValue = values.nextElement().validFitness;
            curKey = keys.nextElement();
            if (curValue > max) {
                max = curValue;
                bestConfig = curKey;
            }
        }
        return bestConfig;
    }

    /**
     * Function will execute parallel model construction and learning process of input configurations. This function cannot
     * run in parallel mode!
     *
     * @param nodes Input array of TreeNode or FitnessNode.
     * @return Return array of fitness for input objects.
     */
    protected double[] getFitnessParallel(Object[] nodes, boolean useValidSetOnLearn) {
        int computed = 0;
        LearnThread[] thread = new LearnThread[nodes.length];
        Semaphore activeThreads = new Semaphore(numberOfThreads);
        double[] fitness = new double[nodes.length];
        boolean finished;
        int firstActive = 0;
        long lastCheck = System.currentTimeMillis();
        try {
            while (nodes.length != computed) {
                finished = activeThreads.tryAcquire(maxComputationTimeMs, TimeUnit.MILLISECONDS);

                if (!finished || System.currentTimeMillis() - lastCheck > maxComputationTimeMs) {
                    lastCheck = System.currentTimeMillis();
                    checkThreads(nodes, fitness, thread, firstActive);
                    //do not start a new thread if permit was not given
                    if (!finished) continue;
                }

                thread[computed] = new LearnThread(computed, this, nodes[computed], fitness, activeThreads, useValidSetOnLearn);
                thread[computed].start();
                computed++;
            }
            //wait for last threads
            do {
                finished = activeThreads.tryAcquire(numberOfThreads, maxComputationTimeMs, TimeUnit.MILLISECONDS);
                if (!finished) checkThreads(nodes, fitness, thread, firstActive);
            } while (!finished);
        } catch (InterruptedException e) {
            log.warn("parallel error: " + e.getMessage());
        }
        return fitness;
    }

    /**
     * Checks running threads and kills threads that used more cpu time than maxComputationTimeMs.
     *
     * @param nodes       Array of configurations.
     * @param fitness     Array of results of the computing threads. If the thread is killed NEGATIVE_INFINITY is saved as its output.
     * @param thread      Array of computing threads.
     * @param firstActive Index of first active thread, iteration will start at that point.
     */
    protected void checkThreads(Object[] nodes, double[] fitness, Thread[] thread, int firstActive) {
        ThreadMXBean threadManagement = ManagementFactory.getThreadMXBean();
        long threadTime;
        for (int i = firstActive; i < thread.length; i++) {
            if (thread[i] == null) break;

            if (thread[i].isAlive()) {
                threadTime = threadManagement.getThreadCpuTime(thread[i].getId()) / 1000000;
                if (threadTime > maxComputationTimeMs * 0.9 && !thread[i].isInterrupted()) {
                    log.warn("interrupting thread [" + thread[i].getId() + "] (" + (threadTime / 1000) + "/" + (maxComputationTimeMs / 1000) + "s) " + nodes[i]);
                    thread[i].interrupt();
                    fitness[i] = Double.NEGATIVE_INFINITY;
                    //update cache if possible, to prevent same config evaluation again
                    if (nodes[i] instanceof TreeNode) {
                        HashTableContainer container = new HashTableContainer();
                        container.node = ((TreeNode) nodes[i]).clone();
                        container.validFitness = Double.NEGATIVE_INFINITY;
                        container.testFitness = Double.NEGATIVE_INFINITY;
                        container.occurrences = modelsBeforeCacheUse;
                        cachedConfigs.put(nodes[i].toString(), container);
                    }
                } else if (threadTime > maxComputationTimeMs * 10) {
                    thread[i].stop();
                    log.error("killing thread [" + thread[i].getId() + "] (" + (threadTime / 1000) + "/" + (maxComputationTimeMs / 1000) + "s) " + nodes[i]);
                }
            } else {
                if (i == firstActive) firstActive++;
            }
        }
    }

    public double[] getFitness(Object[] obj) {
        return getFitness(obj, false);
    }

    protected double[] getFitness(Object[] obj, boolean useValidSetOnLearn) {
        if (numberOfThreads > 1) parallelComputation = true;

        double[] fitness = getFitnessParallel(obj, useValidSetOnLearn);

        parallelComputation = false;
        return fitness;
    }

    public double getFitness(TreeNode obj) {
        Fitness fit = new Fitness();
        String key = obj.toString();

        HashTableContainer container;
        if (cachedConfigs.containsKey(key)) {
            container = cachedConfigs.get(key);
            if (cachedConfigs.get(key).occurrences >= modelsBeforeCacheUse) {
                //CRITICAL SECTION
                if (parallelComputation) getLock();
                cacheUse++;
                if (parallelComputation) parallelLock.release();

                log.debug("CACHED: " + container.validFitness + " " + key);
                return container.validFitness;
            }
        } else if (obj instanceof InnerTreeNode) {
            int models = getNumberOfModels((InnerFitnessNode) obj.node);
            if (models > maxModels) {
                log.debug("node computation too complex");
                return Double.NEGATIVE_INFINITY;
            }
        }

        try {
            if (parallelComputation) log.debug("COMPUTING[" + Thread.currentThread().getId() + "]: " + key);
            else log.debug("COMPUTING: " + key);

            fit = getModelFitness(obj.node);

            if (parallelComputation) log.debug("FITNESS[" + Thread.currentThread().getId() + "]: " + fit.validFitness);
            else log.debug("FITNESS: " + fit.validFitness);
        } catch (OutOfMemoryError e) {
            fit.validFitness = Double.NEGATIVE_INFINITY;
            fit.testFitness = Double.NEGATIVE_INFINITY;
            log.debug("no enough memory");
        } catch (Exception e) {
            logLearningError(e, key);
            return Double.NEGATIVE_INFINITY;
        }
        //CRITICAL SECTION
        if (parallelComputation) getLock();
        updateHashTable(obj, key, fit);
        updateBestSolution(key, fit.validFitness);
        if (parallelComputation) parallelLock.release();

        return fit.validFitness;
    }

    protected void updateHashTable(TreeNode obj, String key, Fitness newFitness) {
        HashTableContainer container;
        if (cachedConfigs.containsKey(key)) {
            container = cachedConfigs.get(key);
            int RMSEcertainty;
            double deviationFromAvg = Math.abs(container.validFitness - newFitness.validFitness);
            if (deviationFromAvg < Math.abs(container.validFitness) * 0.1)
                RMSEcertainty = 1; //small dispersion from average
            else if (deviationFromAvg > Math.abs(container.validFitness) * 0.5)
                RMSEcertainty = -1; //large dispersion from average
            else RMSEcertainty = 0; //normal dispersion from average

            container.validFitness = (container.validFitness * container.occurrences + newFitness.validFitness) / (container.occurrences + 1);
            container.testFitness = (container.testFitness * container.occurrences + newFitness.testFitness) / (container.occurrences + 1);
            container.occurrences = container.occurrences + 1 + RMSEcertainty;
        } else {
            container = new HashTableContainer();
            container.node = obj.clone();
            container.validFitness = newFitness.validFitness;
            container.testFitness = newFitness.testFitness;
            container.occurrences = 1;
        }
        if (newFitness.validFitness == Double.NEGATIVE_INFINITY) container.occurrences = modelsBeforeCacheUse;

        modelsComputed++;
        cachedConfigs.put(key, container);
    }

    protected void updateBestSolution(String key, double newValidFitness) {
        if (newValidFitness > bestConfigFitness) {
            bestConfig = key;
            bestConfigFitness = newValidFitness;
            //if best models RMSE drops, find out if there is a better config
        } else if (key.equals(bestConfig) && newValidFitness != bestConfigFitness) {
            bestConfig = findBestConfig();
            bestConfigFitness = cachedConfigs.get(bestConfig).validFitness;
        }
    }

    /**
     * Retrieves lock to shared context variables. It ignores interruption, because no complex computations are made after lock is called.
     */
    protected void getLock() {
        parallelLock.acquireUninterruptibly();
    }

    protected int getNumberOfModels(InnerFitnessNode node) {
        int sum = 0;
        for (int i = 0; i < node.getNodesNumber(); i++) {
            if (node.getNode(i) instanceof InnerFitnessNode) {
                sum += getNumberOfModels((InnerFitnessNode) node.getNode(i));
            } else {
                sum++;
            }
        }
        return sum / node.getNodesNumber();
    }

    protected void divideLearnData(int[] indexes) {
        //MEMORY ALLOCATION
        int dataNum = indexes.length;

        testIndex = new int[(int) (dataNum * testDataPercent)];
        validIndex = new int[(int) Math.round(dataNum * validDataPercent)];
        learnIndex = new int[dataNum - validIndex.length - testIndex.length];

        MyRandom rnd = new MyRandom(dataNum);
        for (int i = 0; i < learnIndex.length; i++) {
            learnIndex[i] = indexes[rnd.getRandom(dataNum)];
        }

        for (int i = 0; i < validIndex.length; i++) {
            validIndex[i] = indexes[rnd.getRandom(dataNum)];
        }

        finalLearnIndex = mergeArrays(learnIndex, validIndex);

        for (int i = 0; i < testIndex.length; i++) {
            testIndex[i] = indexes[rnd.getRandom(dataNum)];
        }
    }

    protected void divideLearnData() {
        int[] indexes = new int[data.getInstanceNumber()];
        for (int i = 0; i < indexes.length; i++) indexes[i] = i;
        divideLearnData(indexes);
    }

    //todo:dodelat
    public double getGenotypeDistance(FitnessNode node1, FitnessNode node2) {
        return 0;
    }

    public double getPhenotypeDistance(FitnessNode node1, FitnessNode node2) {
        return 0;
    }

    public void printContextData() {
        Enumeration<String> keys = cachedConfigs.keys();
        Enumeration<HashTableContainer> values = cachedConfigs.elements();

        HashTableContainer curValue;
        String curKey;
        int i = 0;
        while (values.hasMoreElements()) {
            curValue = values.nextElement();
            curKey = keys.nextElement();
            System.out.println(i + ";" + curKey + ";" + curValue.validFitness + ";" + curValue.testFitness);
            i++;
        }
    }

    /**
     * Set and get functions
     */

    public ElapsedTime getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(ElapsedTime elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getMaxComputationTimeMs() {
        return maxComputationTimeMs;
    }

    public void setMaxComputationTimeMs(long maxComputationTimeMs) {
        this.maxComputationTimeMs = maxComputationTimeMs;
    }

    public double getTestFitness(TreeNode node) {
        getFitness(node);
        return cachedConfigs.get(node.toString()).testFitness;
    }

    public double verifyTestFitness(TreeNode node) {
        verifyFitness(node);
        return cachedConfigs.get(node.toString()).testFitness;
    }

    /**
     * Performs additional computation of fitness of best node (defined by performance on test data) to verify result stability.
     * Needs to search entire hash table each step -> slow to compute.
     */
    public void verifyBestTestNode() {
        //perform verifying until bestNode does not changes after that
        TreeNode newNode = getBestNodeOnTestData();
        TreeNode bestNode;
        do {
            bestNode = newNode;
            verifyTestFitness(bestNode);
            newNode = getBestNodeOnTestData();
        } while (!newNode.toString().equals(bestNode.toString()));
    }

    /**
     * @return Returns best node according to TEST data.
     */
    public TreeNode getBestNodeOnTestData() {
        //if there are no test data, use valid data instead
        if (testIndex.length == 0) return getBestNode();

        Enumeration<HashTableContainer> values = cachedConfigs.elements();

        double max = Double.NEGATIVE_INFINITY;
        HashTableContainer curNode;
        TreeNode bestConfig = null;
        while (values.hasMoreElements()) {
            curNode = values.nextElement();
            if (curNode.testFitness > max) {
                max = curNode.testFitness;
                bestConfig = curNode.node;
            }
        }
        return bestConfig;
    }


    public int getModelsComputed() {
        return modelsComputed;
    }

    public int getCacheUse() {
        return cacheUse;
    }

    public int getNumberOfUniqueConfigs() {
        return cachedConfigs.size();
    }

    /**
     * @return Returns VALID fitness of the best config.
     */
    public double getBestFitness() {
        return bestConfigFitness;
    }

    /**
     * @return Returns best node according to VALID data.
     */
    public TreeNode getBestNode() {
        if (bestConfig != null) return cachedConfigs.get(bestConfig).node;
        else return null;
    }

    /**
     * @return Returns TEST fitness of best model. Valid fitness for best model is not available.
     */
    public double getBestModelTestFitness() {
        return bestFitness;
    }

    /**
     * @param data Input data that will be inserted into cache.
     */
    public void setData(HashTableContainer[] data) {
        for (int i = 0; i < data.length; i++) {
            cachedConfigs.put(data[i].node.toString(), data[i]);
        }
    }

    /**
     * @param key Key used in the hash table
     * @return Returns container of individual identified by key.
     */
    public HashTableContainer getIndividual(String key) {
        return cachedConfigs.get(key);
    }

    /**
     * @return Returns array of records from cache.
     */
    public HashTableContainer[] getData() {
        Enumeration<HashTableContainer> values = cachedConfigs.elements();
        List<HashTableContainer> records = new ArrayList<HashTableContainer>();

        while (values.hasMoreElements()) {
            records.add(values.nextElement());
        }
        return records.toArray(new HashTableContainer[]{});
    }

    public int getMaxModels() {
        return maxModels;
    }

    public void setMaxModels(int maxModels) {
        this.maxModels = maxModels;
    }

    public FitnessNode getBestModelConfig() {
        return bestModelConfig;
    }

    public void setTestDataPercent(double testDataPercent) {
        this.testDataPercent = testDataPercent;
    }

    public int getModelsBeforeCacheUse() {
        return modelsBeforeCacheUse;
    }

    public void setModelsBeforeCacheUse(int modelsBeforeCacheUse) {
        this.modelsBeforeCacheUse = modelsBeforeCacheUse;
    }

    public int[] getLearnValidIndex() {
        return finalLearnIndex;
    }

    public int[] getTestIndex() {
        return testIndex;
    }

    public void setValidDataPercent(double validDataPercent) {
        this.validDataPercent = validDataPercent;
    }

    public static void setNumberOfThreads(int numOfThreads) {
        numberOfThreads = numOfThreads;
    }

    public static int getNumberOfThreads() {
        return numberOfThreads;
    }

    public AbstractGameData getDataSet() {
        return data;
    }

}
