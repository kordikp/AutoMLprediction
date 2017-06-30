package game.classifiers.single;


import configuration.classifiers.single.DecisionTreeClassifierConfig;
//import game.cSerialization.CCodeUtils;
//import game.cSerialization.XMLBuildUtils;
import game.classifiers.ClassifierBase;
import game.evolution.treeEvolution.evolutionControl.EvolutionUtils;
import org.apache.commons.lang.ArrayUtils;
import weka.core.Utils;

import java.util.Random;

public class DecisionTreeClassifier extends ClassifierBase {

    protected DecisionTreeNode successor;

    //parameters
    protected int m_KValue = 0;
    protected double m_MinNum = 1.0;
    protected int m_MaxDepth = 100;

    public void learn() {
        // Make sure K value is in range
        if (m_KValue > inputs - 1) m_KValue = inputs - 1;
        if (m_KValue < 1) m_KValue = (int) Utils.log2(inputs) + 1;

        // Figure out appropriate datasets
        //---------------removed

        // Create the attribute indices window
        int[] attIndicesWindow = new int[inputs];
        for (int i = 0; i < inputs; i++) {
          attIndicesWindow[i] = i;
        }

        // Compute initial class counts
        int[] classes = convertOutputData(target);
        double[] classProbs = new double[outputs];
        for (int i = 0; i < learning_vectors; i++) {
            classProbs[classes[i]]++;
        }

        // Build tree
        Random rnd = new Random();
        successor = new DecisionTreeNode();
        successor.setKValue(m_KValue);
        successor.setMaxDepth(getMaxDepth());
        successor.setMinLeafSize(m_MinNum);
        successor.learn(inputVect,classes, classProbs, attIndicesWindow, rnd, 0,outputs);

        // Backfit if required
        //---------------removed

        postLearnActions();
    }

    public static int getMaxIndex(double[] from) {
        int result = 0;
        for( int i = 1; i < from.length; ++i ) {
            if( from[i] > from[result] ) {
                result = i;
            }
        }
        return result;
    }

    public static int[] convertOutputData(double[][] targetVariables) {
        int[] classes = new int[targetVariables.length];
        for(int i=0;i<targetVariables.length;i++) {
            classes[i] = getMaxIndex(targetVariables[i]);
        }
        return classes;
    }


    @Override
    public Class getConfigClass() {
        return DecisionTreeClassifierConfig.class;
    }

    @Override
    public void relearn() {
        learn();
    }

    @Override
    public double[] getOutputProbabilities(double[] input_vector) {
        return successor.getOutputProbabilities(input_vector);
    }

 /*   @Override
    public String toCCode(StringBuilder code, StringBuilder xml) {
        String functionName = CCodeUtils.getUniqueFunctionName(this.getClass());
        CCodeUtils.getCClassificationHeader(functionName, inputs, code,"in");

        code.append("double * res = new double[").append(outputs).append("];\n");
        code.append("for(int i=0;i<").append(outputs).append(";i++){\n");
            code.append("res[i] = 0;\n");
        code.append("}\n");

        successor.toCCode(code);

        //normalize
        code.append("\ndouble sum=0;\n");
        code.append("for(int i=0;i<").append(outputs).append(";i++){\n");
            code.append("sum += res[i];\n");
        code.append("}\n");

        code.append("for(int i=0;i<").append(outputs).append(";i++){\n");
            code.append("res[i] /= sum;\n");
        code.append("}\n");
        code.append("return res;\n");

        code.append("}\n");

        XMLBuildUtils.outputXML(xml, this, functionName);
        return functionName;
    }
*/
    /**
     * GET and SET methods
     */

    public int getMaxDepth() { return m_MaxDepth; }
    public void setMaxDepth(int maxDepth) { m_MaxDepth = maxDepth; }

    public int getKValue() { return m_KValue; }
    public void setKValue(int kValue) { m_KValue = kValue; }

    @Override
    public String[] getEquations(String[] inputEquation) {
        return new String[]{""}; //FIXME: TODO:
    }
}
