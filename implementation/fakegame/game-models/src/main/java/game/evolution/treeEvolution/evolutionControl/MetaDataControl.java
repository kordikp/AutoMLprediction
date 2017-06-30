package game.evolution.treeEvolution.evolutionControl;


import configuration.ConfigurationFactory;
import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.ConnectableClassifierConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.models.ConnectableModelConfig;
import configuration.models.ModelConfig;
import configuration.models.single.*;
//import configuration.models.single.rapidMiner.RapidNeuralNetConfig;
import game.data.AbstractGameData;
import game.data.MiningType;
import game.evolution.treeEvolution.*;
import game.evolution.treeEvolution.context.*;
import game.utils.Utils;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Class to handle meta data processing for evolution.
 */
public class MetaDataControl {

    private DataRepository db;
    AbstractGameData originalData;
    private Object[] oldMetaData;
    private Object[] newMetaData;
    private FitnessContainer[] oldData;
    private int loadedId = -1;
    private Logger log;
    private int fitnessFormat = 1;
    private ElapsedTime elapsedTime;
    private long secondsDuration;

    private String fileName;

    //maximum number of configs in a file
    private int maxSavedSolutions = 30;
    //number of configs evaluated and saved
    private int evaluatedSolutions;

    //todo: udelat to ze se vyhodnoti evaluatedSolutions configu, prida se k jiz ulozenym datum a ulozi se maxSavedSolutions nejlepsich
    public MetaDataControl(AbstractGameData originalData, String fileName, int evaluatedSolutions, ElapsedTime elapsedTime, long secondsDuration) {
        if (originalData.getDataType() == MiningType.REGRESSION) fitnessFormat = -1;
        this.fileName = fileName;
        this.elapsedTime = elapsedTime;
        this.secondsDuration = secondsDuration;
        this.originalData = originalData;
        setEvaluatedSolutions(evaluatedSolutions);

        db = new FileRepository();
        log = Logger.getLogger(this.getClass());
    }

    public void printSettings() {
        log.info("-----------------------------------------------");
        log.info("META DATA CONFIGURATION:");
        log.info("-----------------------------------------------");
        log.info("Number of in detail evaluated solutions: " + evaluatedSolutions);
        log.info("-----------------------------------------------");
    }

    /**
     * Half of them will be loaded from most similar data, other half
     * will be selected as one best individual per each other data sorted by similarity.
     *
     * @return Returns number of individuals given by generationSize.
     */
    public FitnessNode[] loadData(TreeEvolution evolution, FitnessContextBase context) {
        String[][] metaData = db.loadMetaData();
        newMetaData = computeMetaData(evolution, context);
        int[] indexes = sortBySimilarity(metaData, newMetaData, evolution.getGenerationSize());
        //if we don't have appropriate data to load return null
        if (indexes.length == 0) {
            loadedId = 0;
            return null;
        }

        loadedId = Integer.parseInt(metaData[indexes[0]][0]);
        oldMetaData = new String[metaData[indexes[0]].length - 1];
        System.arraycopy(metaData[indexes[0]], 1, oldMetaData, 0, oldMetaData.length);
        FitnessContainer[] data = db.loadData(loadedId);

        //set half of the init generation as best individuals from most similar meta data
        int initGenSize = Math.min(data.length + indexes.length - 1, evolution.getGenerationSize());
        FitnessNode[] initGeneration = new FitnessNode[initGenSize];
        int largerHalf = (int) Math.ceil((double) initGeneration.length / 2);
        //add more from most similar dataset if there aren't any more datasets
        largerHalf += Math.max((initGeneration.length - largerHalf) - indexes.length + 1, 0);

        log.info(largerHalf + " CONFIGS LOADED FROM: " + metaData[indexes[0]][1]);
        oldData = new FitnessContainer[largerHalf];
        for (int i = 0; i < largerHalf; i++) {
            log.info(data[i].finalTestFitness + " " + data[i].node);
            initGeneration[i] = data[i].node;
            oldData[i] = data[i];
        }
        //set smaller half as best individual from best N meta data files
        int idx = 1;
        for (int i = largerHalf; i < initGeneration.length; i++) {
            data = db.loadData(Integer.parseInt(metaData[indexes[idx]][0]));
            initGeneration[i] = data[0].node;

            log.info("1 CONFIG LOADED FROM: " + metaData[indexes[idx]][1] + " " + data[0].finalTestFitness + " " + data[0].node);
            idx++;
        }
        fixInputOptimization(initGeneration);
        return initGeneration;
    }

    /**
     * This function goes after all individuals in generation and checks if there is a input optimizing config, in that case
     * it changes number of optimized inputs to actual value from the data.
     *
     * @param initGeneration Initial generation loaded from database.
     */
    private void fixInputOptimization(FitnessNode[] initGeneration) {
        FitnessNode successor;
        for (int i = 0; i < initGeneration.length; i++) {
            if (initGeneration[i] instanceof ConnectableClassifierConfig) {
                ConnectableClassifierConfig cfg = (ConnectableClassifierConfig) initGeneration[i];
                if (cfg.getSelectedInputs().length != originalData.getINumber()) {
                    successor = cfg.getNode(0);
                    cfg = new ConnectableClassifierConfig(originalData.getINumber());
                    cfg.addNode(successor);
                    initGeneration[i] = cfg;
                }
            } else if (initGeneration[i] instanceof ConnectableModelConfig) {
                ConnectableModelConfig cfg = (ConnectableModelConfig) initGeneration[i];
                if (cfg.getSelectedInputs().length != originalData.getINumber()) {
                    successor = cfg.getNode(0);
                    cfg = new ConnectableModelConfig(originalData.getINumber());
                    cfg.addNode(successor);
                    initGeneration[i] = cfg;
                }
            }
        }
    }


    public FitnessContainer saveData(TreeEvolution evolution, FitnessContextBase context, FitnessContextBase testContext) {
        log.info("saving data");
        if (loadedId == -1) {
            loadData(evolution, context);
        }

        //compare new and old RMSE values of individuals and determine difference of data based on model performance
        double avgDeviation = 0;
        if (loadedId != 0) {
            double newFitness;
            for (int i = 0; i < oldData.length; i++) {
                newFitness = getFinalTestFitness(evolution.createTemplate(oldData[i].node), context, testContext, 0)[0];
                avgDeviation += Utils.sigmoid(Math.abs(newFitness - oldData[i].finalTestFitness) / oldData[i].finalTestFitness, 2);
            }
            avgDeviation = avgDeviation / oldData.length;
        }
        log.info("MODEL OUTPUT DEVIATION: " + avgDeviation);

        FitnessContainer[] prepareData = prepareData(evolution, context, testContext);

        //meta data are exactly the same -> overwrite data (they are same if they does have same metaData values or same filename)
        if (metaArrayEquality(newMetaData, oldMetaData, 1)) {
            //if there are more metadata with same data link and deviation is too big, save the data separately
            if (avgDeviation > 0.5) {
                int id = db.updateMetaData(loadedId, newMetaData);
                db.saveData(id, prepareData);
                if (id == loadedId) log.info("META-DATA:no change REFERENCE:no change DATA:overwritten");
                else log.info("META-DATA:no change REFERENCE:to new data DATA:new");
            } else {
                db.saveData(loadedId, prepareData);
                log.info("META-DATA:no change REFERENCE:no change DATA:overwritten");
            }
        } else if (loadedId == 0 || avgDeviation > 0.1) { //save as new data
            int id = db.saveMetaData(newMetaData);
            db.saveData(id, prepareData);
            log.info("META-DATA:new REFERENCE:to new data DATA:new");
        } else { //save new meta data and set their pointer to same data file as loaded meta data (they are similar)
            db.saveMetaData(loadedId, newMetaData);
            db.saveData(loadedId, prepareData);
            log.info("META-DATA:new REFERENCE:to existing data DATA:no change");
        }

        log.debug("SAVING CONFIGURATIONS:");
        for (int i = 0; i < prepareData.length; i++) {
            log.debug(prepareData[i].finalTestFitness * fitnessFormat + " " + prepareData[i].node.toString());
        }

        return prepareData[0];
    }

    public String printResults(FitnessContainer bestConfig) {
        String time = Long.toString(System.currentTimeMillis());
        log.fatal(elapsedTime.getTimeS() + ";" + bestConfig.finalTestFitness * fitnessFormat + ";" + bestConfig.node.toString() + ";" + time);

        return time;
    }

    public void saveBestConfig(FitnessNode bestNode, String id) {
        if (bestNode instanceof ModelConfig) {
            ((ModelConfig) bestNode).setName(bestNode.toString());
        } else if (bestNode instanceof ClassifierConfig) {
            ((ClassifierConfig) bestNode).setName(bestNode.toString());
        }

        String dataName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
        String savedFileName = "/tmp/FG-bestCFG-" + dataName + "_" + id + ".txt";
        log.info("BEST CONFIG SAVED TO FILE: " + savedFileName);
        ConfigurationFactory.saveConfiguration(bestNode, savedFileName);
    }

    public String printExtensiveTestResults(FitnessContainer config, int maxModels) {
        //size of validation set
        double validDataPercent;
        int dataNum = originalData.getInstanceNumber();
        if (dataNum <= 100) {
            //use only 1 instance as valid set
            validDataPercent = 1.0 / originalData.getInstanceNumber();
        } else if (dataNum > 1000) {
            validDataPercent = 0.2;
        } else if (dataNum > 500) {
            //linear increase
            validDataPercent = 0.0002 * dataNum;
        } else {
            //exponential in interval <100,500>
            validDataPercent = (0.4 * Math.exp(dataNum * 0.00965)) / dataNum;
        }
        double validSetSizeBonus = (dataNum * 0.2) / (dataNum * validDataPercent);
        maxModels = (int) (maxModels * validSetSizeBonus);

        FitnessContextBase extensiveTestContext = getTestContext(validDataPercent, maxModels);

        double fitnessSum = 0;
        double[] fitness;
        double fitnessAvg = 0;
        int batchSize = Math.max((int) (5 * validSetSizeBonus), 10);
        ArrayList<Double> allFitness = new ArrayList<Double>();
        int maxComputed = batchSize;
        //initialize batch group
        FitnessNode[] batchGroup = new FitnessNode[batchSize];
        //make shallow copies because config is used for read only
        for (int i = 0; i < batchGroup.length; i++) batchGroup[i] = config.node;

        while (maxComputed <= maxModels) {
            if (allFitness.size() != 0) fitnessAvg = fitnessSum / allFitness.size();
            log.info("TESTING (" + batchSize + "x) " + fitnessAvg * fitnessFormat + " " + config.node.toString() + " fitnessSum = "+fitnessSum);

            fitness = extensiveTestContext.getFitness(batchGroup);

            for (int i = 0; i < fitness.length; i++) {
                if (fitness[i] == Double.NEGATIVE_INFINITY) { //reduce maximum number of models computed if model cannot be created for some reasons
                    maxModels = maxModels / 2;
                    continue;
                }
                fitnessSum += fitness[i];
                allFitness.add(fitness[i]);
            }

            maxComputed = maxComputed + batchSize;
            if (Math.abs(fitnessAvg - fitnessSum / allFitness.size()) <= fitnessAvg * 0.01) break;
        }
        //compute dispersion
        double dispersion = 0;
        fitnessAvg = fitnessSum / allFitness.size();
        for (int i = 0; i < allFitness.size(); i++) {
            dispersion += Math.pow(allFitness.get(i) - fitnessAvg, 2);
        }
        dispersion = dispersion / allFitness.size();

        String time = Long.toString(System.currentTimeMillis());
        log.info(elapsedTime.getTimeS() + ";" + config.finalTestFitness * fitnessFormat + ";"
                + allFitness.size() + "x" + (int) (validDataPercent * dataNum) + ";" + fitnessAvg * fitnessFormat + ";" + dispersion + ";" + config.node.toString() + ";" + time);

        return time;
    }

    private FitnessContextBase getTestContext(double validDataPercent, int maxModels) {
        FitnessContextBase extensiveTestContext;
        if (originalData.getDetailedDataType() == MiningType.ORDER_PREDICTION) {
            extensiveTestContext = new OrderNFoldClassifierContext();
        } else if (originalData.getDataType() == MiningType.CLASSIFICATION) {
            extensiveTestContext = new NFoldClassifierContext();
        } else if (originalData.getDataType() == MiningType.REGRESSION) {
            extensiveTestContext = new NFoldModelContext();
        } else {
            return null;
        }
        extensiveTestContext.setValidDataPercent(validDataPercent);
        extensiveTestContext.setTestDataPercent(0);
        extensiveTestContext.setModelsBeforeCacheUse(maxModels);
        extensiveTestContext.setMaxComputationTimeMs(EvolutionUtils.getMaxComputationTimeMs(secondsDuration, originalData) * 2);
        extensiveTestContext.init(originalData);
        extensiveTestContext.setElapsedTime(elapsedTime);

        return extensiveTestContext;
    }

    private Object[] computeMetaData(TreeEvolution evolution, FitnessContextBase context) {
        ArrayList<Object> metaData = new ArrayList<Object>();
        //extract file name
        int fileFromIndex = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
        metaData.add(fileName.substring(fileFromIndex + 1));

        FitnessNode[] leafNodes = new FitnessNode[0];
        //add classification or regression attribute + get leaf nodes configurations
        if (originalData.getDataType() == MiningType.REGRESSION) {
            metaData.add("R");
            leafNodes = getRegressionLeafNodes();
        } else if (originalData.getDataType() == MiningType.CLASSIFICATION) {
            metaData.add("C");
            leafNodes = getClassificationLeafNodes();
        }
        EvolutionUtils.addInputOptimizer(leafNodes, originalData);

        double[] fitness = new double[leafNodes.length];
        int[] indexes = new int[leafNodes.length];
        for (int i = 0; i < leafNodes.length; i++) {
            indexes[i] = i;
            fitness[i] = -1 * context.verifyFitness(evolution.createTemplate(leafNodes[i]));
        }

        Utils.quicksort(fitness, indexes, 0, indexes.length - 1);

        int[] positions = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) positions[indexes[i]] = i;
        for (int i = 0; i < positions.length; i++) metaData.add((double) positions[i]);
        //data statistics
        metaData.add(originalData.getInstanceNumber());
        metaData.add(originalData.getINumber());
        metaData.add(originalData.getONumber());

        String infoOutput = "META DATA: ";
        for (int i = 0; i < metaData.size(); i++) {
            infoOutput += metaData.get(i).toString() + ";";
        }
        log.info(infoOutput);

        return metaData.toArray(new Object[metaData.size()]);
    }

    private FitnessNode[] getClassificationLeafNodes() {
        FitnessNode[] regLeafNodes = getRegressionLeafNodes();
        FitnessNode[] clsLeafNodes = new FitnessNode[regLeafNodes.length + 5];

        ClassifierModelConfig cModel;
        for (int i = 0; i < regLeafNodes.length; i++) {
            cModel = new ClassifierModelConfig();
            cModel.addNode(regLeafNodes[i]);
            clsLeafNodes[i] = cModel;
        }

    /*    clsLeafNodes[regLeafNodes.length] = new RapidNaiveBayesConfig();
        clsLeafNodes[regLeafNodes.length+1] = new RapidDecisionTreeConfig();
        clsLeafNodes[regLeafNodes.length+2] = new RapidKNNConfig();
        clsLeafNodes[regLeafNodes.length+3] = new RapidSVMConfig();
        clsLeafNodes[regLeafNodes.length+4] = new RapidNeuralNetClassifierConfig();
*/
        return clsLeafNodes;
    }

    private FitnessNode[] getRegressionLeafNodes() {
        FitnessNode[] regLeafNodes = new FitnessNode[6];
        regLeafNodes[0] = new LinearModelConfig();
        regLeafNodes[1] = new PolynomialModelConfig();
        regLeafNodes[2] = new SigmoidModelConfig();
        regLeafNodes[3] = new GaussianModelConfig();
        regLeafNodes[4] = new SineModelConfig();
        regLeafNodes[5] = new ExpModelConfig();
        //       regLeafNodes[6] = new RapidNeuralNetConfig();

        return regLeafNodes;
    }


    private int[] sortBySimilarity(String[][] db, Object[] data, int firstN) {
        double[] similarity = new double[db.length];
        int infinityCounter = 0;
        for (int i = 0; i < db.length; i++) {
            //if names are equal similarity is 0 (they are same)
            if (db[i][1].equals(data[0])) {
                similarity[i] = 0;
                continue;
            }

            for (int j = 1; j < data.length; j++) {
                similarity[i] += computeDistance(db[i][j + 1], data[j]);
            }
            if (similarity[i] == Double.POSITIVE_INFINITY) infinityCounter++;
        }

        //discard infinity distances from the list
        int[] indexes = new int[db.length - infinityCounter];
        for (int i = 0; i < indexes.length; i++) {
            if (similarity[i] != Double.POSITIVE_INFINITY) indexes[i] = i;
        }

        if (indexes.length > 1) Utils.quicksort(similarity, indexes, 0, indexes.length - 1);

        int min = Math.min(firstN, indexes.length);
        for (int i = 0; i < min; i++) {
            log.info(db[indexes[i]][1] + " " + similarity[indexes[i]]);
        }
        return indexes;
    }

    private double computeDistance(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            if (o1.equals(o2)) return 0;
            else return Double.POSITIVE_INFINITY;
        } else {
            double value1 = getDoubleValue(o1);
            double value2 = getDoubleValue(o2);
            double divisor = Math.max(Math.abs(value1), Math.abs(value2));
            if (divisor == 0) divisor = 1;
            return Math.abs(value1 - value2) / divisor;
        }
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public FitnessContainer getBestConfig(TreeEvolution evolution, FitnessContextBase context, FitnessContextBase testContext) {
        log.info("verifying best config");
        return prepareData(evolution, context, testContext)[0];
    }

    private FitnessContainer[] prepareData(TreeEvolution evolution, FitnessContextBase context, FitnessContextBase testContext) {
        HashTableContainer data[] = context.getData();

        double[] sortBy = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            sortBy[i] = -1 * (data[i].testFitness + data[i].validFitness / 2);
        }
        //get firstN best configs sorted by test fitness
        int[] indexes;
        int firstN = Math.min(evaluatedSolutions, data.length);
        indexes = Utils.insertSort(sortBy, firstN);

        double penalty = getPenalty(sortBy, indexes);
        double[] fitnessResult;
        sortBy = new double[firstN];
        FitnessContainer fit = new FitnessContainer();
        FitnessContainer[] trimmedData = new FitnessContainer[firstN];
        //add best model
        fit.node = context.getBestModelPredefinedConfig();
        fitnessResult = getFinalTestFitness(evolution.createTemplate(fit.node), context, testContext, penalty);
        trimmedData[0] = fit;
        fit.finalTestFitness = fitnessResult[0];
        sortBy[0] = -1 * fitnessResult[1];
        //transform array to FitnessContainers and take only firstN from data
        for (int i = 1; i < trimmedData.length; i++) {
            fit = new FitnessContainer();
            fitnessResult = getFinalTestFitness(data[indexes[i - 1]].node, context, testContext, penalty);
            fit.node = data[indexes[i - 1]].node.node;
            trimmedData[i] = fit;

            fit.finalTestFitness = fitnessResult[0];
            sortBy[i] = -1 * fitnessResult[1];
        }
        //sort again by final test fitness
        for (int i = 0; i < indexes.length; i++) indexes[i] = i;
        Utils.quicksort(sortBy, indexes, 0, sortBy.length - 1);

        FitnessContainer[] sortedByTestRMSE = new FitnessContainer[trimmedData.length];
        for (int i = 0; i < trimmedData.length; i++) {
            sortedByTestRMSE[i] = trimmedData[indexes[i]];
        }
        return sortedByTestRMSE;
    }

    protected double getPenalty(double[] fitness, int[] sortedIndexes) {
        double min = fitness[sortedIndexes[0]];
        double max = 0;
        //save first number from the end that is not positive infinity as maximum
        for (int i = sortedIndexes.length - 1; i >= 0; i--) {
            if (fitness[sortedIndexes[i]] != Double.POSITIVE_INFINITY) {
                max = fitness[sortedIndexes[i]];
                break;
            }
        }
        return Math.abs(max - min) * 1.1;
    }

    private double[] getFinalTestFitness(TreeNode node, FitnessContextBase context, FitnessContextBase testContext, double penalty) {
        double[] fitness = new double[2];
        fitness[0] = testContext.getFinalTestFitness(node);
        if (fitness[0] == Double.NEGATIVE_INFINITY) {
            log.warn("Can't create model on test context, using original context " + node.toString());
            context.verifyFitness(node);
            //penalize models that cant be created on test context
            fitness[0] = context.getTestFitness(node);
            fitness[1] = fitness[0] - penalty;
        } else {
            fitness[1] = fitness[0];
        }
        return fitness;
    }

    private boolean metaArrayEquality(Object[] a, Object[] b, int startIndex) {
        if (a == null || b == null) return false;
        //if file names are the same -> treat them as equal
        if (newMetaData[0].equals(oldMetaData[0])) return true;
        if (a.length != b.length) return false;
        for (int i = startIndex; i < a.length; i++) {
            if (!(a[i].equals(b[i]))) return false;
        }
        return true;
    }

    /**
     * Set and get functions
     */

    public void setEvaluatedSolutions(int evaluatedSolutions) {
        if (evaluatedSolutions <= 1) this.evaluatedSolutions = 2;
        else this.evaluatedSolutions = evaluatedSolutions;
    }
}
