/*
 * DataForCorrelationComp.java
 *
 * Created on 23 decembre 2008, 16:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package game.classifiers.neural;

/**
 * @author Puchina
 */
public class DataForCorrelationComp {

    public double[] outputsAverageResidualError;
    public double[][] outputsResidualErrors;
    public double[] candidatesSumValue;
    public double[][] candidatesValues;
    public double[] sumErrors;
    public double sumSqError;

    /**
     * Creates a new instance of DataForCorrelationComp
     */
    public DataForCorrelationComp(int candidatesNumber, int outputsNumber, int trainingSetSize) {
        this.sumSqError = 0;
        this.sumErrors = new double[outputsNumber];
        this.candidatesSumValue = new double[candidatesNumber];
        this.candidatesValues = new double[candidatesNumber][trainingSetSize];
        this.outputsAverageResidualError = new double[outputsNumber];
        this.outputsResidualErrors = new double[outputsNumber][trainingSetSize];
    }

}
