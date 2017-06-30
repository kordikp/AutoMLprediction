package game.evolution.treeEvolution.evolutionControl;

import configuration.CfgTemplate;
//import configuration.Slider;
import configuration.classifiers.*;
import configuration.models.*;
import game.data.*;
import org.ytoh.configurations.annotations.SelectionSet;
import org.ytoh.configurations.ui.CheckBox;
import configuration.classifiers.ensemble.EnsembleClassifierConfig;
import configuration.classifiers.single.ClassifierModelConfig;
import configuration.evolution.EvolutionControlConfig;
import game.evolution.treeEvolution.*;
import game.evolution.treeEvolution.context.*;
import game.utils.Utils;
import org.apache.log4j.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Class for advanced controlling evolution
 */
public class EvolutionControl {
    private TreeEvolution evolution;
    private TreeEvolution varOptEvolution;
    private FitnessContextBase context;
    private AbstractGameData data;
    private AbstractGameData originalData;
    private FitnessNode[] templates;
    private FitnessNode[] initGeneration;
    private String fileName;
    Logger log;

    private int[] dataMap;

    private boolean dataReduced = false;
    private boolean useTestSet = true;
    //-1 disabled; 0 disabled but can be enabled during evolution process; 1 enabled
    private int allowInputOptimization = -1;
    private long lastFitnessChange = 0;

    private boolean extensiveTesting = true;
    private boolean useMetaData = false;
    private int instanceThreshold = 500;
    //if there are more inputs perform data reduction before evolution
    private int inputThreshold = 200;
    private int convergenceGenerations;

    private FitnessContextBase testContext;
    FitnessContainer bestConfig;

    private long secondsDuration;
    private ElapsedTime elapsedTime;

    public EvolutionControl(String fileName) {
        log = Logger.getLogger(this.getClass());
        this.fileName = fileName;
    }

    public EvolutionControl(GameData data) {
        originalData = (AbstractGameData) data;
        log = Logger.getLogger(this.getClass());
        fileName = "./data" + System.currentTimeMillis() + ".txt";
    }

    public void init(CfgTemplate cfg) {
        EvolutionControlConfig config = (EvolutionControlConfig) cfg;
        useTestSet = config.getUseTestSet();
        secondsDuration = config.getRunTime();
    }

    /**
     * Runs the automated evolution process.
     */
    public void autoRun() {
        //INIT
        originalData = loadMiningData();
        setVariables(secondsDuration);
        printSettings(secondsDuration);
        //PREPARATION
        PreprocessingControl preprocessing = new PreprocessingControl();
        data = preprocessing.run(originalData, instanceThreshold, inputThreshold);
        if (originalData != data) dataReduced = true;

        context = loadDefaultContext(data);
        //todo: dodelat paralelizaci - predelat distribuci dat a rapid modely
        FitnessContextBase.setNumberOfThreads(1);
        context.init(data);

        templates = loadDefaultTemplates();
        evolution = initEvolution(templates);

        if (dataReduced) updateDataSetIndexes(preprocessing.getReducedIndexes());

        MetaDataControl meta = initMetaDataControl();
        if (useMetaData) initGeneration = meta.loadData(evolution, context);
        if (initGeneration == null) initGeneration = loadDefaultInitGeneration();
        EvolutionUtils.addInputOptimizer(initGeneration, data);
        //RUN
        evolution.init(initGeneration, context);
        loadVarOptFromAnnotations(evolution);
        applyMutationRestrictions();
        if (allowInputOptimization == 1) initInputCoEvolution(evolution);

        runEvolution(secondsDuration);
        //RESULTS
        testContext = loadTestContext();
        if (useMetaData) bestConfig = meta.saveData(evolution, context, testContext);
        else bestConfig = meta.getBestConfig(evolution, context, testContext);

        String recordId;
        if (extensiveTesting) recordId = meta.printExtensiveTestResults(bestConfig, 40);
        else recordId = meta.printResults(bestConfig);

       meta.saveBestConfig(bestConfig.node, recordId); // FIXME: potential h2o issues + relative path
    }

    private void printSettings(long secondsDuration) {
        log.info("-----------------------------------------------");
        log.info("AUTOMATED DATA MINING CONFIGURATION:");
        log.info("-----------------------------------------------");
        log.info("File: " + fileName);
        log.info("Time[h:m:s]: " + (secondsDuration / 3600) % 60 + ":" + (secondsDuration / 60) % 60 + ":" + secondsDuration % 60);
        log.info("Instance number threshold: " + instanceThreshold);
        log.info("Input number threshold: " + inputThreshold);
        log.info("Generations to converge: " + convergenceGenerations);
        log.info("Output level: " + log.getEffectiveLevel());
        log.info("-----------------------------------------------");
    }

    private void setVariables(long secondsDuration) {
        if (originalData.getInstanceNumber() > instanceThreshold) {
            instanceThreshold += Math.min(originalData.getInstanceNumber(), (instanceThreshold / 100) * (int) Math.sqrt(secondsDuration));
        }
        if (originalData.getINumber() > inputThreshold) {
            inputThreshold += Math.min(originalData.getINumber(), (inputThreshold / 100) * (int) Math.sqrt(secondsDuration));
        }
        //enable optimization straight away if data has more inputs
        if (allowInputOptimization == 0 && originalData.getINumber() > 10) allowInputOptimization = 1;
        //for thread cpu time computation
        elapsedTime = new ElapsedTime();
        convergenceGenerations = 10 + (int) Math.pow(secondsDuration, 1.0 / 3);
        //leave some reserve for overhead
        this.secondsDuration = (long) (secondsDuration * 0.9);
    }

    private void runEvolution(long secondsDuration) {
        long remainingTime = secondsDuration;
        int currentMaxGen = 1;
        int varsToOptimize = 0;
        //run structure optimization until time is up leaving some time for variable evolution based on number of optimized variables
        while (remainingTime > Math.sqrt(secondsDuration) * Math.min(varsToOptimize, 3)) {
            currentMaxGen = runEvolutionForGivenTime(currentMaxGen, remainingTime / 2);
            varsToOptimize = numVariablesToOptimize(context.getBestNode());
            remainingTime = secondsDuration - elapsedTime.getTimeS();
        }
        lastFitnessChange = 0;
        //run variable optimization for best solution if it has variables to optimize
        if (remainingTime > 0 && numVariablesToOptimize(context.getBestNode()) > 0) {
            initVarEvolution();
            runVarEvolutionForGivenTime(1, remainingTime);
        }
        context.verifyBestTestNode();
    }

    private int runEvolutionForGivenTime(int startMaxGen, long secondsDuration) {
        int maxGen = startMaxGen;
        double oldFitness = 0;
        double fitness = 0;
        long endTime = elapsedTime.getTimeS() + secondsDuration;
        while (elapsedTime.getTimeS() < endTime) {
            //detect convergence
            if (oldFitness == fitness) {
                //number of generations without fitness update reach for convergence
                if ((evolution.getCurrentGeneration() - lastFitnessChange) > convergenceGenerations) {
                    convergence(evolution);
                    lastFitnessChange = maxGen;
                }
            } else { //best solution changed -> reset last change time
                lastFitnessChange = maxGen;
            }

            oldFitness = fitness;
            fitness = context.getBestFitness();

            setEvolutionParameters(maxGen);
            evolution.run();
            maxGen++;
        }
        evolution.verifyBestSolution();
        return maxGen;
    }

    private void convergence(TreeEvolution evolution) {
        if (allowInputOptimization == 0) {
            log.info("CONVERGENCE DETECTED: " + convergenceGenerations + " generations without best solution update");
            allowInputOptimization(evolution);
            allowInputOptimization = 1;
            log.info("input optimization allowed");
        } else if (dataReduced && Runtime.getRuntime().totalMemory() * 2 < Runtime.getRuntime().maxMemory()) { //increase data size only if at least twice amount of memory is available
            log.info("CONVERGENCE DETECTED: " + convergenceGenerations + " generations without best solution update");
            increaseDataSize(evolution);
        } else if (context.getModelsBeforeCacheUse() < 10) { //if we cant increase data size, increase number of models needed to be computed to be certain of their fitness
            log.info("CONVERGENCE DETECTED: " + convergenceGenerations + " generations without best solution update");
            int newValue = context.getModelsBeforeCacheUse() * 2;
            if (newValue > 10) newValue = 10;
            context.setModelsBeforeCacheUse(newValue);
            log.info("model computations needed increased to " + context.getModelsBeforeCacheUse());
        }
    }

    //todo: pri zvetseni poctu vstupu vznikne problem s connectable classifier, pokud predtim doslo k redukci vstupu
    private void increaseDataSize(TreeEvolution evolution) {
        HashTableContainer ctxData[] = context.getData();

        double[] sortBy = new double[ctxData.length];
        for (int i = 0; i < ctxData.length; i++) {
            sortBy[i] = -1 * ctxData[i].validFitness;
        }

        int[] indexes = Utils.insertSort(sortBy, evolution.getGenerationSize());

        for (int i = 0; i < indexes.length; i++) {
            evolution.setIndividual(i, ctxData[indexes[i]].node.clone());
        }

        instanceThreshold = Math.min(instanceThreshold * 2, originalData.getInstanceNumber());
        inputThreshold = Math.min(inputThreshold * 2, originalData.getINumber());

        int[] reducedIndexes;
        if (instanceThreshold >= originalData.getInstanceNumber() && inputThreshold >= originalData.getINumber()) {
            data = originalData;
            dataReduced = false;
            reducedIndexes = new int[data.getInstanceNumber()];
            for (int i = 0; i < reducedIndexes.length; i++) reducedIndexes[i] = i;
            log.info("USING ORIGINAL DATA");
        } else {
            //generate new greater sample from data
            PreprocessingControl preprocessing = new PreprocessingControl();
            data = preprocessing.run(originalData, instanceThreshold, inputThreshold);
            reducedIndexes = preprocessing.getReducedIndexes();
        }

        context = loadDefaultContext(data);
        int[] validLearnIndex = getIndexesOfType(reducedIndexes, 1);
        int[] testIndex = getIndexesOfType(reducedIndexes, 2);
        context.init(data, validLearnIndex, testIndex);

        if (dataReduced) updateDataSetIndexes(reducedIndexes);
        else dataMap = null;

        evolution.setFitnessContext(context);
    }

    private int[] getIndexesOfType(int[] sourceIndexes, int type) {
        int[] indexes = new int[sourceIndexes.length];
        int idx = 0;
        for (int i = 0; i < sourceIndexes.length; i++) {
            if (dataMap[sourceIndexes[i]] == type) indexes[idx++] = i;
        }

        int[] trimmedIndexes = new int[idx];
        for (int i = 0; i < trimmedIndexes.length; i++) {
            trimmedIndexes[i] = indexes[i];
        }
        return trimmedIndexes;
    }

    /**
     * Updates the dataMap by current context learn+valid and test sets using reducedIndexes as map from context index space
     * to originalData index space. Values used in the data map:
     * 0 - means that use of the given vector is undefined and can be used in any set.
     * 1 - vector belongs to learn+valid set and cannot be used for different set.
     * 2 - vector belongs to test set and cannot be used for different set.
     *
     * @param reducedIndexes
     */
    private void updateDataSetIndexes(int[] reducedIndexes) {
        if (dataMap == null) dataMap = new int[originalData.getInstanceNumber()];
        int[] learnValidIndexes = context.getLearnValidIndex();
        for (int i = 0; i < learnValidIndexes.length; i++) {
            dataMap[reducedIndexes[learnValidIndexes[i]]] = 1;
        }

        int[] testIndexes = context.getTestIndex();
        for (int i = 0; i < testIndexes.length; i++) {
            dataMap[reducedIndexes[testIndexes[i]]] = 2;
        }
    }

    private int runVarEvolutionForGivenTime(int startMaxGen, long secondsDuration) {
        int maxGen = startMaxGen;
        long genAvgTime = 0;
        double oldFitness = 0;
        double fitness = 0;
        long prevGenTime = elapsedTime.getTimeS();
        long curGenTime;
        long endTime = elapsedTime.getTimeS() + secondsDuration;
        while (elapsedTime.getTimeS() + genAvgTime * 0.5 < endTime) {
            //detect convergence
            if (oldFitness == fitness) {
                //number of generations without fitness update reach for convergence
                if ((varOptEvolution.getCurrentGeneration() - lastFitnessChange) > convergenceGenerations) {
                    convergence(varOptEvolution);
                    lastFitnessChange = maxGen;
                }
            } else { //best solution changed -> reset last change time
                lastFitnessChange = maxGen;
            }

            oldFitness = fitness;
            fitness = context.getBestFitness();

            setVarEvolutionParameters(maxGen);
            varOptEvolution.run();
            maxGen++;

            curGenTime = elapsedTime.getTimeS();
            genAvgTime = (genAvgTime + (curGenTime - prevGenTime)) / 2;
            prevGenTime = curGenTime;
        }
        varOptEvolution.verifyBestSolution();
        return maxGen;
    }

    private int numVariablesToOptimize(TreeNode node) {
        int numVars = 0;
        if (node.templateNode.getMethods.length > 0) numVars += node.templateNode.getMethods.length;

        if (node instanceof InnerTreeNode) {
            InnerTreeNode innerNode = (InnerTreeNode) node;
            for (int i = 0; i < innerNode.getNodesNumber(); i++) {
                numVars += numVariablesToOptimize(innerNode.getNode(i));
            }
        }

        return numVars;
    }

    public CfgTemplate getBestConfig() {
        return (CfgTemplate) bestConfig.node;
    }

    public Object getBestModel() {
        if (data.getDataType() == MiningType.CLASSIFICATION) {
            return ((ClassifierContextBase) context).getBestModel();
        } else {
            return ((ModelContextBase) context).getBestModel();
        }
    }

    /**
     * Sets evolution parameters based on given time.
     */
    private void setEvolutionParameters(int maxGenerations) {
        evolution.setMaxGenerations(maxGenerations);
        evolution.setMaxTreeDepth((int) Math.round(Math.pow((double) maxGenerations / 5, 1.0 / 3)) + 1);
        evolution.setGenerationSize((int) Math.round(Math.sqrt(maxGenerations)) + 9);
        context.setMaxModels((int) (maxGenerations * 5 * Math.sqrt(data.getONumber())));
        evolution.setLocalMutationThreshold(1);
    }

    private void setVarEvolutionParameters(int maxGenerations) {
        varOptEvolution.setMaxGenerations(maxGenerations);

        maxGenerations = maxGenerations / 2;
        varOptEvolution.setGenerationSize((int) Math.round(Math.sqrt(maxGenerations)) + 5);
    }

    /**
     * Apply restrictions to the nodes, restricting to which nodes can templates mutate. It is used mainly for enabling the
     * construction of trees composed from regression and classification ensembles in classification task.
     */
    private void applyMutationRestrictions() {
        if (data.getDataType() == MiningType.REGRESSION) applyRegressionRestrictions();
        else if (data.getDataType() == MiningType.CLASSIFICATION) applyClassificationRestrictions();
    }

    private void applyRegressionRestrictions() {
        FitnessNode template;

        ModelUnits mod = ModelUnits.getInstance();
        ModelEnsembleUnits modEnsembles = ModelEnsembleUnits.getInstance();
        FitnessNode[] modelsAndEnsembles = new FitnessNode[mod.getCount() + modEnsembles.getCount()];

        int idx;
        //load all base models
        for (idx = 0; idx < mod.getCount(); idx++) {
            modelsAndEnsembles[idx] = (FitnessNode) mod.getUnitConfig(idx);
        }
        //load all regression ensembles
        for (int i = 0; i < modEnsembles.getCount(); i++) {
            modelsAndEnsembles[idx] = (FitnessNode) modEnsembles.getUnitConfig(i);
            idx++;
        }

        for (int i = 0; i < evolution.getNumberOfTemplates(); i++) {
            template = evolution.getTemplate(i);
            if (template instanceof ModelOperator) {
                evolution.setCanMutateTo(i, new FitnessNode[0]);
                evolution.setTemplateDepthWeight(i, 0);
            } else if (template instanceof ModelConfig) {
                evolution.setCanMutateTo(i, modelsAndEnsembles);
            }
        }
    }

    private void applyClassificationRestrictions() {
        ModelUnits mod = ModelUnits.getInstance();
        ModelEnsembleUnits modEnsembles = ModelEnsembleUnits.getInstance();
        ClassifierEnsembleUnits clsEnsembles = ClassifierEnsembleUnits.getInstance();
        ClassifierUnits classifiers = ClassifierUnits.getInstance();

        FitnessNode[] modelsAndEnsembles = new FitnessNode[mod.getCount() + modEnsembles.getCount()];
        FitnessNode[] models = new FitnessNode[mod.getCount()];
        FitnessNode[] clsModelsAndClsEnsembles = new FitnessNode[mod.getCount() + clsEnsembles.getCount() + classifiers.getCount()];

        int idx;
        //load all base models
        FitnessNode cfg;
        ClassifierModelConfig cModel;
        for (idx = 0; idx < mod.getCount(); idx++) {
            cfg = (FitnessNode) mod.getUnitConfig(idx);
            modelsAndEnsembles[idx] = cfg;
            models[idx] = cfg;
            //create classifierModel envelope for leaf nodes of classifier tree
            cModel = new ClassifierModelConfig();
            cModel.addNode(cfg);
            clsModelsAndClsEnsembles[idx] = cModel;
        }
        //load all regression ensembles
        for (int i = 0; i < modEnsembles.getCount(); i++) {
            modelsAndEnsembles[idx] = (FitnessNode) modEnsembles.getUnitConfig(i);
            idx++;
        }

        idx = mod.getCount();
        //load all classifiers
        for (int i = 0; i < classifiers.getCount(); i++) {
            clsModelsAndClsEnsembles[idx] = (FitnessNode) classifiers.getUnitConfig(i);
            idx++;
        }
        //load all classification ensembles
        for (int i = 0; i < clsEnsembles.getCount(); i++) {
            clsModelsAndClsEnsembles[idx] = (FitnessNode) clsEnsembles.getUnitConfig(i);
            idx++;
        }

        //apply restrictions
        FitnessNode template;
        for (int i = 0; i < evolution.getNumberOfTemplates(); i++) {
            template = evolution.getTemplate(i);
            if (template instanceof ModelOperator || template instanceof ClassifierOperator) {
                evolution.setCanMutateTo(i, new FitnessNode[0]);
                evolution.setTemplateDepthWeight(i, 0);
            } else if (template instanceof ClassifierModelConfig) {
                evolution.setCanMutateTo(i, clsModelsAndClsEnsembles);
                evolution.setAddMutationLeaves(i, models);
                evolution.setNodeGrowingMutation(i, true);
                evolution.setTemplateDepthWeight(i, 0);
            } else if (template instanceof ModelConfig) {
                evolution.setCanMutateTo(i, modelsAndEnsembles);
            } else if (template instanceof ClassifierConfig) {
                evolution.setCanMutateTo(i, clsModelsAndClsEnsembles);
            }
        }
    }

    private FitnessNode[] loadDefaultTemplates() {
        if (data.getDataType() == MiningType.REGRESSION) return loadRegressionTemplates();
        else if (data.getDataType() == MiningType.CLASSIFICATION) return loadClassificationTemplates();
        else return null;
    }

    /**
     * Modifies current generation of evolution adding Connectable classifier config to each individual to allow
     * input optimization.
     */
    private void allowInputOptimization(TreeEvolution evol) {
        for (int i = 0; i < templates.length; i++) {
            if (templates[i] instanceof ConnectableClassifierConfig || templates[i] instanceof ConnectableModelConfig) {
                enableVarOptimization(evol, templates[i], i);
                break;
            }
        }
        initInputCoEvolution(evol);
    }

    private FitnessNode[] loadRegressionTemplates() {
        ModelUnits models = ModelUnits.getInstance();
        ModelEnsembleUnits modEnsembles = ModelEnsembleUnits.getInstance();

        FitnessNode[] templates = new FitnessNode[models.getCount() + modEnsembles.getCount() + 1];
        int idx;
        //load all base models
        for (idx = 0; idx < models.getCount(); idx++) {
            templates[idx] = (FitnessNode) models.getUnitConfig(idx);
        }
        //load all ensembles
        for (int i = 0; i < modEnsembles.getCount(); i++) {
            templates[idx] = (FitnessNode) modEnsembles.getUnitConfig(i);
            idx++;
        }
        //connectable model for input optimization
        templates[idx] = EvolutionUtils.createInputOptimizer(data);

        return templates;
    }

    private FitnessNode[] loadDefaultInitGeneration() {
        if (data.getDataType() == MiningType.CLASSIFICATION) return createClsInitGeneration();
        else return createLeafInitGeneration();
    }

    private FitnessNode[] createClsInitGeneration() {
        ArrayList<FitnessNode> initGen = new ArrayList<FitnessNode>();
        for (int i = 0; i < templates.length; i++) {
            if (templates[i] instanceof ClassifierConfig && !(templates[i] instanceof EnsembleClassifierConfig) && !(templates[i] instanceof ClassifierOperator)) {
                initGen.add(templates[i]);
            }
        }
        return initGen.toArray(new FitnessNode[initGen.size()]);
    }

    private FitnessNode[] createLeafInitGeneration() {
        ArrayList<FitnessNode> initGen = new ArrayList<FitnessNode>();
        for (int i = 0; i < templates.length; i++) {
            if (!(templates[i] instanceof InnerFitnessNode)) {
                initGen.add(templates[i]);
            }
        }
        return initGen.toArray(new FitnessNode[initGen.size()]);
    }

    private FitnessNode[] loadClassificationTemplates() {
        ModelUnits models = ModelUnits.getInstance();
        ModelEnsembleUnits modEnsembles = ModelEnsembleUnits.getInstance();
        ClassifierEnsembleUnits clsEnsembles = ClassifierEnsembleUnits.getInstance();
        ClassifierUnits classifiers = ClassifierUnits.getInstance();

        FitnessNode[] templates = new FitnessNode[models.getCount() * 2 + modEnsembles.getCount() + clsEnsembles.getCount() + classifiers.getCount() + 1];

        int idx = 0;
        ClassifierModelConfig cModel;
        //load all base models
        for (int i = 0; i < models.getCount(); i++) {
            templates[idx] = (FitnessNode) models.getUnitConfig(i);
            cModel = new ClassifierModelConfig();
            cModel.setModelsNumber(data.getONumber());
            cModel.addNode(templates[idx]);
            idx++;

            templates[idx] = cModel;
            idx++;
        }
        //load all classifiers
        for (int i = 0; i < classifiers.getCount(); i++) {
            templates[idx] = (FitnessNode) classifiers.getUnitConfig(i);
            idx++;
        }
        //load all regression ensembles
        for (int i = 0; i < modEnsembles.getCount(); i++) {
            templates[idx] = (FitnessNode) modEnsembles.getUnitConfig(i);
            idx++;
        }
        //load all classification ensembles
        for (int i = 0; i < clsEnsembles.getCount(); i++) {
            templates[idx] = (FitnessNode) clsEnsembles.getUnitConfig(i);
            idx++;
        }
        //connectable classifier for input optimization
        templates[idx] = EvolutionUtils.createInputOptimizer(data);

        return templates;
    }

    /**
     * Loads data from file and determines the mining type of the task.
     *
     * @return Returns data loaded from file.
     */
    private AbstractGameData loadMiningData() {
        AbstractGameData data;
        if (originalData != null) {
            data = originalData;
        } else {
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            fileType = fileType.toLowerCase();
            if (fileType.equals("arff")) {
                data = new ArffGameData(fileName);
            } else {
                data = new FileGameData(fileName);
            }
        }
        return data;
    }

    /**
     * Loads a test context on whole data set respecting the previous validLearn and test index division.
     *
     * @return Returns test context.
     */
    private FitnessContextBase loadTestContext() {
        FitnessContextBase testContext;
        if (dataReduced) {
            testContext = loadDefaultContext(originalData);

            int[] reducedIndexes = new int[originalData.getInstanceNumber()];
            for (int i = 0; i < reducedIndexes.length; i++) reducedIndexes[i] = i;

            int[] validLearnIndex = getIndexesOfType(reducedIndexes, 1);
            int[] testIndex = getIndexesOfType(reducedIndexes, 2);
            testContext.init(originalData, validLearnIndex, testIndex);
        } else {
            testContext = context;
        }
        return testContext;
    }

    /**
     * Heuristic method to load best context based on data size.
     *
     * @param data Input data.
     * @return Returns context.
     */
    private FitnessContextBase loadDefaultContext(AbstractGameData data) {
        FitnessContextBase context;
        if (data.getDetailedDataType() == MiningType.ORDER_PREDICTION) {
            if (data.getInstanceNumber() > 5000) context = new OrderNFoldClassifierContext();
            else context = new OrderADClassifierContext();
        } else if (data.getDataType() == MiningType.REGRESSION) {
            if (data.getInstanceNumber() > 5000) context = new NFoldModelContext();
            else context = new AreaDivideModelContext();
        } else if (data.getDataType() == MiningType.CLASSIFICATION) {
            if (data.getInstanceNumber() > 5000) context = new NFoldClassifierContext();
            else context = new AreaDivideClassifierContext();
        } else {
            return null;
        }
        if (useTestSet == false) context.setTestDataPercent(0);
        context.setMaxComputationTimeMs(EvolutionUtils.getMaxComputationTimeMs(secondsDuration, data));
        context.setElapsedTime(elapsedTime);

        return context;
    }

    /**
     * Automatically loads variables for optimization from annotations.
     *
     * @param evol Evolution where the optimization of the variables will be set.
     */
    private void loadVarOptFromAnnotations(TreeEvolution evol) {
        for (int i = 0; i < templates.length; i++) {
            if (allowInputOptimization != 1 && (templates[i] instanceof ConnectableClassifierConfig || templates[i] instanceof ConnectableModelConfig))
                continue;

            enableVarOptimization(evol, templates[i], i);
        }
    }

    /**
     * Enables optimization of all variables loaded from annotation of given configuration.
     *
     * @param evol          Input evolution.
     * @param node          Input configuration.
     * @param templateIndex Index of the template(configuration) within input evolution.
     */
    private void enableVarOptimization(TreeEvolution evol, FitnessNode node, int templateIndex) {
        ArrayList<Field> vars = getAllClassVariables(node.getClass());

        ArrayList<String> method = new ArrayList<String>();
        ArrayList<Class> variableType = new ArrayList<Class>();
        ArrayList<Double> minValues = new ArrayList<Double>();
        ArrayList<Double> maxValues = new ArrayList<Double>();

        //  Slider slider;
        CheckBox checkbox;
        SelectionSet selectionSet;

        for (int j = 0; j < vars.size(); j++) {
//            slider = vars.get(j).getAnnotation(Slider.class);
/*            if (slider != null) {
                method.add(vars.get(j).getName());
                variableType.add(vars.get(j).getType());
                minValues.add(1.0*slider.min()/slider.multiplicity());
                maxValues.add(1.0*slider.max()/slider.multiplicity());
            }
*/ //todo repair
            checkbox = vars.get(j).getAnnotation(CheckBox.class);
            if (checkbox != null) {
                method.add(vars.get(j).getName());
                variableType.add(vars.get(j).getType());
                minValues.add(0.0);
                maxValues.add(1.0);
            }

            selectionSet = vars.get(j).getAnnotation(SelectionSet.class);
            if (selectionSet != null) {
                method.add(vars.get(j).getName());
                variableType.add(vars.get(j).getType());
                minValues.add(0.0);
                maxValues.add(0.0);
            }

        }

        int num = method.size();
        double[] varMinValue = new double[num];
        double[] varMaxValue = new double[num];
        for (int j = 0; j < num; j++) {
            varMinValue[j] = minValues.get(j);
            varMaxValue[j] = maxValues.get(j);
        }
        //set variables for optimize
        evol.setMethodsToOptimalize(templateIndex, method.toArray(new String[num]), variableType.toArray(new Class[num]));
        evol.setMinVarValueForOptimalize(templateIndex, varMinValue);
        evol.setMaxVarValueForOptimalize(templateIndex, varMaxValue);
    }

    /**
     * Recursive method to traverse whole tree of parent classes and collects all variables inherited from them.
     *
     * @param cls Input class.
     * @return Returns array of variables that input class has.
     */
    private ArrayList<Field> getAllClassVariables(Class cls) {
        Field[] variables = cls.getDeclaredFields();
        ArrayList<Field> result = new ArrayList<Field>();
        for (int i = 0; i < variables.length; i++) {
            result.add(variables[i]);
        }

        if (cls.getSuperclass() != null) result.addAll(getAllClassVariables(cls.getSuperclass()));
        return result;
    }

    /**
     * Methods initializing more complex objects
     */

    private MetaDataControl initMetaDataControl() {
        int evaluatedSolutions = 20;
        if (useMetaData == false) evaluatedSolutions = Math.min(10, (int) Math.sqrt(secondsDuration / 10) + 2);

        MetaDataControl meta = new MetaDataControl(originalData, fileName, evaluatedSolutions, elapsedTime, secondsDuration);
        meta.printSettings();
        return meta;
    }

    private TreeEvolution initEvolution(FitnessNode[] templates) {
        evolution = new TreeCoEvolution(templates);

        if (data.getDataType() == MiningType.CLASSIFICATION) evolution.setOutputFitnessFormat(true);
        setEvolutionParameters(1);

        return evolution;
    }

    private void initInputCoEvolution(TreeEvolution evolution) {
        if (!(evolution instanceof TreeCoEvolution)) return;

        int generationSize = 100;
        log.info("initializing input coevolution");
        boolean[][] templates = new boolean[1][data.getINumber()];
        //all inputs active
        for (int i = 0; i < templates[0].length; i++) templates[0][i] = true;

        CoEvolution coev = new ArrayCoEvolution(templates, generationSize);

        TreeCoEvolution treeCoev = (TreeCoEvolution) evolution;
        treeCoev.setCoEvolution(coev);
        if (data.getDataType() == MiningType.CLASSIFICATION)
            treeCoev.setCoEvolutionTarget(coev, ConnectableClassifierConfig.class, "selectedInputs");
        else if (data.getDataType() == MiningType.REGRESSION)
            treeCoev.setCoEvolutionTarget(coev, ConnectableModelConfig.class, "selectedInputs");
    }

    private void initVarEvolution() {
        varOptEvolution = new TreeCoEvolution(templates);

        if (data.getDataType() == MiningType.CLASSIFICATION) varOptEvolution.setOutputFitnessFormat(true);

        FitnessNode[] initGen = new FitnessNode[1];
        initGen[0] = context.getBestNode().node;

        //setVarEvolutionParameters();
        varOptEvolution.init(initGen, context);
        loadVarOptFromAnnotations(varOptEvolution);

        varOptEvolution.setNodeAddMutationProb(0);
        varOptEvolution.setNodeChangeMutationProb(0);
        varOptEvolution.setVariableMutationProb(0.7);

        if (allowInputOptimization == 1)
            ((TreeCoEvolution) varOptEvolution).setCoEvolution(((TreeCoEvolution) evolution).getCoEvolution());
    }

    /**
     * Get and set functions
     */

    /**
     * Defines if evolution will be run with learn/valid sets only or with learn/valid/test set.
     * Set to false if you want to perform your own evaluation on your test set not given to evolution.
     * Own evaluation may lead to worse values, because different methods are used for selecting these sets during evolution
     * and because evolution picks final best model based on performance on test data.
     *
     * @param useTestSet Boolean if test set should be used.
     */
    public void useTestSet(boolean useTestSet) {
        this.useTestSet = useTestSet;
    }

    public TreeEvolution getEvolutionAlgorithm() {
        return evolution;
    }

    public long getSecondsDuration() {
        return secondsDuration;
    }

    public void setRunTime(long secondsDuration) {
        this.secondsDuration = secondsDuration;
    }

    public ElapsedTime getElapsedTime() {
        return elapsedTime;
    }

}
