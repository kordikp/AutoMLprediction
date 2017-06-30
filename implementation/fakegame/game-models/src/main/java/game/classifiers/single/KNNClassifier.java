package game.classifiers.single;


import configuration.classifiers.ClassifierConfig;
import configuration.classifiers.single.KNNClassifierConfig;
//import game.cSerialization.CCodeUtils;
//import game.cSerialization.XMLBuildUtils;
import game.classifiers.ClassifierBase;

import game.evolution.treeEvolution.evolutionControl.EvolutionUtils;
import game.tools.distance.DistanceMeasure;
import game.tools.distance.DistancesWithIndexes;


import org.ytoh.configurations.ui.SelectionSetModel;
//import utils.UtilsCommon;

import java.lang.reflect.Constructor;

public class KNNClassifier extends ClassifierBase {

    public class LearnException extends RuntimeException {


        public LearnException() {
            super();
        }

        public LearnException(String message) {
            super(message);
        }

    }
    //parameters
    private int nearestNeighbours;
    private boolean weightByDistance;
    private SelectionSetModel<String> measureType;

    private double[][] learnDataInput;
    private int[] learnDataClasses;

    private DistanceMeasure distance;

    public void init(ClassifierConfig cfg) {
        super.init(cfg);
        KNNClassifierConfig config = (KNNClassifierConfig) cfg;

        nearestNeighbours = config.getNearestNeighbours();
        weightByDistance = config.getWeightedVote();
        measureType = cloneSelectionSet(config.getMeasureType());
    }

    @Override
    public void learn() {
        learnDataInput = inputVect;
        learnDataClasses = convertOutputData(target);
        //initialize distance computation
        try {
            Class theClass = Class.forName("game.tools.distance." + measureType.getEnabledElements(String.class)[0]);
            Constructor constructor = theClass.getConstructor(double[][].class);
            distance = (DistanceMeasure) constructor.newInstance(new Object[]{learnDataInput});
        } catch (Exception e) {
            throw new LearnException(e.getMessage());
        }
        postLearnActions();
    }

    @Override
    public void relearn() {
        learn();
    }

    @Override
    public double[] getOutputProbabilities(double[] input_vector) {
        DistancesWithIndexes distanceResult = distance.getDistanceToNearest(input_vector, nearestNeighbours);
        double[] distances = distanceResult.distances;
        int[] indexes = distanceResult.indexes;

        double[] output = new double[outputs];
        if (weightByDistance) {
            double totalDistance = 0;
            for (int i = 0; i < distances.length; i++) {
                totalDistance += distances[i];
            }

            double totalSimilarity;
            if (totalDistance == 0) {
                totalDistance = 1;
                totalSimilarity = nearestNeighbours;
            } else {
                totalSimilarity = Math.max(nearestNeighbours - 1, 1);
            }

            for (int i = 0; i < indexes.length; i++) {
                output[learnDataClasses[indexes[i]]] += (1d - distances[i] / totalDistance) / totalSimilarity;
            }
        } else {
            double weight = 1 / nearestNeighbours;
            for (int i = 0; i < indexes.length; i++) {
                output[learnDataClasses[indexes[i]]] += weight;
            }
        }
        return output;
    }

    public ClassifierConfig getConfig() {
        KNNClassifierConfig cfg = (KNNClassifierConfig) super.getConfig();
        cfg.setNearestNeighbours(nearestNeighbours);
        cfg.setWeightedVote(weightByDistance);
        cfg.setMeasureType(cloneSelectionSet(measureType));
        return cfg;
    }

    @Override
    public Class getConfigClass() {
        return KNNClassifierConfig.class;
    }

    /*
        public String toCCode(StringBuilder code, StringBuilder xml) {
            code.append("#include \"").append(CCodeUtils.getClassificationModelPath()).append("KNN.h\"\n");
            String functionName = CCodeUtils.getUniqueFunctionName(this.getClass());
            CCodeUtils.getCClassificationHeader(functionName, inputs, code);

            String distanceFunctionName = measureType.getEnabledElements(String.class)[0];
            distanceFunctionName = distanceFunctionName.substring(0,1).toLowerCase() + distanceFunctionName.substring(1);

            CCodeUtils.convertArray(learnDataInput,"learningVectorsInput",code);
            CCodeUtils.convertArray(learnDataClasses,"learningVectorsClasses",code);

            code.append("return knnClassifierOutput<").append(inputs).append(",")
                    .append(learnDataInput.length).append(",")
                    .append(nearestNeighbours).append(",")
                    .append(outputs)
                    .append(">(input,learningVectorsInput,learningVectorsClasses,")
                    .append(distanceFunctionName).append(",")
                    .append(weightByDistance).append(");\n");
            code.append("}\n");

            XMLBuildUtils.outputXML(xml, this, functionName);
            return functionName;
        }*/
    @Override
    public String[] getEquations(String[] inputEquation) {
        return new String[]{""}; //FIXME: TODO:
    }

    public static int getMaxIndex(double[] from) {
        int result = 0;
        for (int i = 1; i < from.length; ++i) {
            if (from[i] > from[result]) {
                result = i;
            }
        }
        return result;
    }

    public static int[] convertOutputData(double[][] targetVariables) {
        int[] classes = new int[targetVariables.length];
        for (int i = 0; i < targetVariables.length; i++) {
            classes[i] = getMaxIndex(targetVariables[i]);
        }
        return classes;
    }

    public static<T> SelectionSetModel<T> cloneSelectionSet(SelectionSetModel<T> oldSet) {
        SelectionSetModel<T> newSet = new SelectionSetModel<T>(oldSet.getAllElements());
        newSet.disableAllElements();
        newSet.enableElementIndices(oldSet.getEnableElementIndices());
        return newSet;
    }
}
