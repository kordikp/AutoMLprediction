package game.classifiers.single;


import weka.core.Utils;

import java.io.Serializable;
import java.util.Random;

public class DecisionTreeNode implements Serializable{
    protected DecisionTreeNode[] m_Successors;
    protected int m_Attribute = -1;
    protected double m_SplitPoint = Double.NaN;
    protected double[] m_ClassDistribution = null;
    protected double[] m_Prop = null;

    //parameters
    protected int m_KValue = 0;
    protected double m_MinNum = 1.0;
    protected int m_MaxDepth = 100;
    protected int outputsNumber;

    protected void learn(double[][] data, int[] classes, double[] classProbs, int[] attIndicesWindow, Random random, int depth, int outputs) {
        outputsNumber = outputs;
        // Make leaf if there are no training instances
        if (data.length == 0) {
          m_Attribute = -1;
          m_ClassDistribution = null;
          m_Prop = null;
          return;
        }

        // Check if node doesn't contain enough instances or is pure
        // or maximum depth reached
        m_ClassDistribution = classProbs;

        if (Utils.sum(m_ClassDistribution) < 2 * m_MinNum
            || Utils.eq(m_ClassDistribution[Utils.maxIndex(m_ClassDistribution)], Utils
                .sum(m_ClassDistribution))
                || ((getMaxDepth() > 0) && (depth >= getMaxDepth()))) {
          // Make leaf
          m_Attribute = -1;
          m_Prop = null;
          return;
        }

        // Compute class distributions and value of splitting
        // criterion for each attribute
        double[] vals = new double[data[0].length];
        double[][][] dists = new double[data[0].length][0][0];
        double[][] props = new double[data[0].length][0];
        double[] splits = new double[data[0].length];

        // Investigate K random attributes
        int attIndex;
        int windowSize = attIndicesWindow.length;
        int k = m_KValue;
        boolean gainFound = false;
        while ((windowSize > 0) && (k-- > 0 || !gainFound)) {
              int chosenIndex = random.nextInt(windowSize);
              attIndex = attIndicesWindow[chosenIndex];

              // shift chosen attIndex out of window
              attIndicesWindow[chosenIndex] = attIndicesWindow[windowSize - 1];
              attIndicesWindow[windowSize - 1] = attIndex;
              windowSize--;

              splits[attIndex] = distribution(props, dists, attIndex, data, classes, outputs);
              vals[attIndex] = gain(dists[attIndex],priorVal(dists[attIndex]));

              if (Utils.gr(vals[attIndex], 0)) gainFound = true;
        }

        // Find best attribute
        m_Attribute = Utils.maxIndex(vals);
        double[][] distribution = dists[m_Attribute];

        // Any useful split found?
        if (Utils.gr(vals[m_Attribute], 0)) {
          // Build subtrees
          m_SplitPoint = splits[m_Attribute];
          m_Prop = props[m_Attribute];
          int[][] subsets = splitData(data);
          m_Successors = new DecisionTreeNode[distribution.length];
          for (int i = 0; i < distribution.length; i++) {
                m_Successors[i] = new DecisionTreeNode();
                m_Successors[i].setKValue(m_KValue);
                m_Successors[i].setMinLeafSize(m_MinNum);
                m_Successors[i].setMaxDepth(getMaxDepth());
                m_Successors[i].learn(filterData(data, subsets[i]), filterData(classes, subsets[i]), distribution[i], attIndicesWindow, random, depth + 1,outputs);
          }

          // If all successors are non-empty, we don't need to store the class distribution
          boolean emptySuccessor = false;
          for (int i = 0; i < subsets.length; i++) {
            if (m_Successors[i].m_ClassDistribution == null) {
              emptySuccessor = true;
              break;
            }
          }
          if (!emptySuccessor) {
            m_ClassDistribution = null;
          }
        } else {
          // Make leaf
          m_Attribute = -1;
        }
    }

    protected int[] filterData(int[] data, int[] indexes) {
          int[] filteredData = new int[indexes.length];
          for (int i = 0; i < indexes.length; i++) {
              filteredData[i] = data[indexes[i]];
          }
          return filteredData;
    }

    protected double[][] filterData(double[][] data, int[] indexes) {
          int dimension = data[0].length;
          double[][] filteredData = new double[indexes.length][dimension];
          for (int i = 0; i < indexes.length; i++) {
              System.arraycopy(data[indexes[i]],0,filteredData[i],0,dimension);
          }
          return filteredData;
    }

    protected int[][] splitData(double[][] data) {
        int[][] splitIndexes = new int[2][data.length];

        // Go through the data
        int idx0 = 0;
        int idx1 = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i][m_Attribute] < m_SplitPoint) {
                splitIndexes[0][idx0++] = i;
            } else {
                splitIndexes[1][idx1++] = i;
            }
        }

        //trim the subsets
        int[][] result = new int[2][];
        result[0] = new int[idx0];
        result[1] = new int[idx1];

        System.arraycopy(splitIndexes[0],0,result[0],0,idx0);
        System.arraycopy(splitIndexes[1],0,result[1],0,idx1);

        return result;
    }

    protected double distribution(double[][] props, double[][][] dists, int att, double[][] data, int[] classes, int outputs) {
        int instances = data.length;

        double splitPoint = Double.NaN;

        double[][] currDist = new double[2][outputs];
        double[][] dist = new double[2][outputs];

        // Sort data
        double[] sortBy = new double[instances];
        for (int i = 0; i < instances; i++) sortBy[i] = data[i][att];
        int[] attrIndexes = game.utils.Utils.quickSort(sortBy);

        // Move all instances into second subset
        for (int j = 0; j < instances; j++) {
            currDist[1][classes[j]] += 1;
        }

        // Value before splitting
        double priorVal = priorVal(currDist);

        // Save initial distribution
        for (int j = 0; j < currDist.length; j++) {
            System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
        }

        // Try all possible split points
        double currSplit = data[attrIndexes[0]][att];
        double currVal, bestVal = -Double.MAX_VALUE;
        double[] inst;
        int classIndex;
        for (int i = 0; i < instances; i++) {
            inst = data[attrIndexes[i]];
            // Can we place a sensible split point here?
            if (inst[att] > currSplit) {
                // Compute gain for split point
                currVal = gain(currDist,priorVal);
                // Is the current split point the best point so far?
                if (currVal > bestVal) {
                    // Store value of current point
                    bestVal = currVal;
                    // Save split point
                    splitPoint = (inst[att] + currSplit) / 2.0;
                    // Save distribution
                    for (int j = 0; j < currDist.length; j++) {
                        System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
                    }
                }
            }

            currSplit = inst[att];
            // Shift over the weight
            classIndex = classes[attrIndexes[i]];
            currDist[0][classIndex] += 1;
            currDist[1][classIndex] -= 1;
        }

        // Compute weights for subsets
        props[att] = new double[dist.length];
        for (int k = 0; k < props[att].length; k++) {
          props[att][k] = Utils.sum(dist[k]);
        }
        if (Utils.eq(Utils.sum(props[att]), 0)) {
          for (int k = 0; k < props[att].length; k++) {
            props[att][k] = 1.0 / (double) props[att].length;
          }
        } else {
          Utils.normalize(props[att]);
        }

        // Return distribution and split point
        dists[att] = dist;
        return splitPoint;
    }

    //Math computations
    private static double log2 = Math.log(2);

    protected double gain(double[][] dist, double priorVal) {
        return priorVal - entropyConditionedOnRows(dist);
    }

    protected double priorVal(double[][] dist) {
        return entropyOverColumns(dist);
    }

    public static double entropyConditionedOnRows(double[][] matrix) {
        double returnValue = 0, sumForRow, total = 0;
        for (int i = 0; i < matrix.length; i++) {
            sumForRow = 0;
            for (int j = 0; j < matrix[0].length; j++) {
                returnValue += lnFunc(matrix[i][j]);
                sumForRow += matrix[i][j];
            }
            returnValue = returnValue - lnFunc(sumForRow);
            total += sumForRow;
        }
        if (Utils.eq(total, 0)) {
          return 0;
        }
        return -returnValue / (total * log2);
    }

    public static double entropyOverColumns(double[][] matrix){
        double returnValue = 0, sumForColumn, total = 0;

        for (int j = 0; j < matrix[0].length; j++){
            sumForColumn = 0;
            for (int i = 0; i < matrix.length; i++) {
                sumForColumn += matrix[i][j];
            }
            returnValue = returnValue - lnFunc(sumForColumn);
            total += sumForColumn;
        }
        if (Utils.eq(total, 0)) {
          return 0;
        }
        return (returnValue + lnFunc(total)) / (total * log2);
    }

    private static double lnFunc(double num){
        if (num < 1e-6) {
            return 0;
        } else {
            return num * Math.log(num);
        }
    }

    public double[] getOutputProbabilities(double[] input_vector) {
        if (m_Attribute > -1) {
            double[] returnedDist;
            if (input_vector[m_Attribute] < m_SplitPoint) {
              returnedDist = m_Successors[0].getOutputProbabilities(input_vector);
            } else {
              returnedDist = m_Successors[1].getOutputProbabilities(input_vector);
            }
            return returnedDist;
        } else {
            if (m_ClassDistribution == null) return new double[outputsNumber];
            else return game.utils.Utils.normalizeAndCloneDistribution(m_ClassDistribution);
        }
    }

    public void toCCode(StringBuilder code) {
        if (m_Attribute == -1) {
            if (m_ClassDistribution == null) return;

            double sum=0;
            for (int i=0;i<m_ClassDistribution.length;i++) {
                sum += m_ClassDistribution[i];
            }

            for (int i=0;i<m_ClassDistribution.length;i++) {
                if(m_ClassDistribution[i] != 0) {
                    code.append("res[").append(i).append("]+=").append(m_ClassDistribution[i]/sum).append(";\n");
                }
            }
        } else { //is inner node
            //write tree output
            code.append("if(").append("in[").append(m_Attribute).append("]<").append(m_SplitPoint).append("){\n");
                (m_Successors[0]).toCCode(code);
            code.append("}else{\n");
                (m_Successors[1]).toCCode(code);
            code.append("}");
        }
    }

    /**
     * GET and SET methods
     */

    public int getMaxDepth() { return m_MaxDepth; }
    public void setMaxDepth(int maxDepth) { m_MaxDepth = maxDepth; }

    public int getKValue() { return m_KValue; }
    public void setKValue(int kValue) { m_KValue = kValue; }

    public double getMinLeafSize() { return m_MinNum; }
    public void setMinLeafSize(double minLeafSize) { m_MinNum = minLeafSize; }
}
