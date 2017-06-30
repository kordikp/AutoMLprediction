package game.models;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

/**
 * This class provides the support for the graph representation of the experiments results
 *
 * @author janhrncir
 */
public class Graphs {

    //SETTINGS
    private int COMPARED_MODELS;
    private int INDEPENDENT_VARIABLES; //models and clusters
    private int MODELS_FIELD; //number of the models field in results
    private int CLUSTERS_FIELD; //number of the clusters field in results
    private int COLUMN_WIDTH;

    private double[][] ba = null;
    private double[][] bo = null;
    private double[][][] ec = null;
    private double[][][] ecBV = null;

    /**
     * Results of experiments
     */
    private double[][] results;

    /**
     * Reference to the instance of singleton
     */
    public static Graphs self = new Graphs();

    /**
     * Private constructor - the class is a singleton
     */
    private Graphs() {
    }

    /**
     * Init the graph class
     *
     * @param results               Results of experiments
     * @param COMPARED_MODELS       Compared models
     * @param INDEPENDENT_VARIABLES Independent variables
     * @param MODELS_FIELD          Number of the models' field
     * @param CLUSTERS_FIELD        Number of the clusters' field
     * @param COLUMN_WIDTH          Width of a column in the output file
     */
    public void initGraps(double[][] results, int COMPARED_MODELS, int INDEPENDENT_VARIABLES, int MODELS_FIELD, int CLUSTERS_FIELD, int COLUMN_WIDTH) {
        this.results = results;
        this.COMPARED_MODELS = COMPARED_MODELS;
        this.INDEPENDENT_VARIABLES = INDEPENDENT_VARIABLES;
        this.MODELS_FIELD = MODELS_FIELD;
        this.CLUSTERS_FIELD = CLUSTERS_FIELD;
        this.COLUMN_WIDTH = COLUMN_WIDTH;
    }

    public void loadRMSFromFile(String filename, int degrees, int clusters, int rpc) {
        ba = new double[degrees][];
        bo = new double[degrees][];
        ec = new double[degrees][][];
        ecBV = new double[degrees][][];

        for (int i = 0; i < degrees; i++) {
            ba[i] = new double[clusters];
            bo[i] = new double[clusters];
            ec[i] = new double[clusters][];
            ecBV[i] = new double[clusters][];
            for (int j = 0; j < clusters; j++) {
                ec[i][j] = new double[rpc];
                ecBV[i][j] = new double[rpc];
            }
        }

        File file = new File(filename);
        String line = "";
        String type = "";
        String typeSERE = "";
        int d = 0;
        int c = 0;
        int r = 0;
        double rms = 0.0;

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                while ((line = input.readLine()) != null) {

                    type = line.substring(0, 2);
                    d = Integer.parseInt(line.substring(3, 6));
                    c = Integer.parseInt(line.substring(7, 10));
                    r = Integer.parseInt(line.substring(11, 14)) / c;
                    typeSERE = line.substring(15, 17);
                    rms = Double.parseDouble(line.substring(18));

                    if (type.equals("BA") || type.equals("BO")) { // BA/BO
                        if (type.equals("BA")) {
                            ba[d][c] = rms;
                        } else {
                            bo[d][c] = rms;
                        }
                    } else { // EC
                        if (typeSERE.equals("SR")) {
                            ec[d][c][r] = rms;
                        } else {
                            ecBV[d][c][r] = rms;
                        }
                    }

                    //System.out.format("%s::%3d::%3d::%3d::%s::%18.6f\n", type, d, c, r, typeSERE, rms);
                    //System.out.println(line);
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load data to the two-dimensional array
     *
     * @param filename Filename
     * @param rows     Number of rows
     * @param columns  Number of columns
     * @return Loaded data
     */
    public double[][] loadDataFromFile(String filename, int rows, int columns) {
        double[][] result = new double[rows][];
        File file = new File(filename);
        String line;
        String[] tokens;

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                int row = 0;
                int column;
                while ((line = input.readLine()) != null) {

                    result[row] = new double[columns];
                    tokens = line.split("\\s");
                    column = 0;

                    for (int j = 0; j < tokens.length; j++) {
                        if (!tokens[j].equals("")) {
                            result[row][column] = Double.parseDouble(tokens[j]);
                            column++;
                        }
                    }
                    row++;
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Save two dimensional data to the file
     *
     * @param data     Data to save
     * @param filename Filename
     */
    public void saveDataToFile(double[][] data, String filename) {
        File file = new File(filename);
        String line;
        try {
            Writer output = new BufferedWriter(new FileWriter(file));
            try {
                for (int i = 0; i < data.length; i++) {
                    line = "";
                    for (int j = 0; j < data[i].length; j++) {
                        line = line.concat(String.format(Locale.ENGLISH, "%16f", data[i][j]));
                    }
                    output.write(line + "\n");
                }
            } finally {
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the MSEs from the results
     *
     * @param outputAttribute  Number of the output attribute
     * @param numberOfClusters Number of clusters
     * @param RPC              Records per cluster
     * @return MSEs data
     */
    private double[] getMSEsFromResults(int outputAttribute, int numberOfClusters, int RPC) {
        double[] errors = new double[COMPARED_MODELS];
        int numberOfBaseModels = numberOfClusters * RPC;
        int index;

        //find the appropriate row in data
        for (int i = 0; i < results.length; i++) {
            if ((results[i][0] == numberOfBaseModels) && (results[i][1] == numberOfClusters)) {

                index = INDEPENDENT_VARIABLES + outputAttribute * COMPARED_MODELS;
                for (int j = 0; j < COMPARED_MODELS; j++) {
                    errors[j] = results[i][index];
                    index++;
                }
                break;
            }
        }

        return errors;
    }

    /**
     * Create a data file for the 3D graph of MSE in dependence on the number clusters and base models
     * *
     *
     * @param maxNumberOfClusters  Maximum namber of clusters
     * @param maxRecordsPerCluster Maximum records per cluster
     * @param filename             Output filename
     */
    public void createDataFileMSEOfEC(int maxNumberOfClusters, int maxRecordsPerCluster, String filename) {

        int columns = maxNumberOfClusters * maxRecordsPerCluster;
        double[][] data = new double[maxNumberOfClusters][];
        double[][] dataToSave = new double[maxNumberOfClusters * columns][];
        double[] row;
        int rowIndex = 0;

        for (int i = 0; i < data.length; i++) {
            data[i] = new double[columns];
        }
        for (int i = 0; i < results.length; i++) {
            data[(int) (results[i][1] - 1)][(int) (results[i][0] - 1)] = results[i][2];
        }
        for (int i = 0; i < maxNumberOfClusters; i++) {
            for (int j = 0; j < columns; j++) {
                row = new double[3];
                row[0] = (i + 1);
                row[1] = (j + 1);
                row[2] = (data[i][j] != 0) ? data[i][j] : Double.NaN;
                dataToSave[rowIndex] = row;
                rowIndex++;
            }
        }
        //System.out.printf("result = %s%n", Arrays.deepToString(dataToSave));

        //SAVE DATA
        saveDataToFile(dataToSave, filename);
    }


    /**
     * Create a data file for the 2D graph of MSE in dependence on the number clusters
     *
     * @param outputAttribute     Output attribute
     * @param minNumberOfClusters Minimum number of clusters
     * @param maxNumberOfClusters Maximum number of clusters
     * @param minRPC              Minimum records per cluster
     * @param maxRPC              Maximum records per cluster
     * @param filename            Output filename
     */
    public void createDataFileRPC(int outputAttribute, int minNumberOfClusters, int maxNumberOfClusters, int minRPC, int maxRPC, String filename) {

        //INIT
        int COLUMNS = 1 + 3 + 2;

        //PREPARE DATA
        int rows = (maxNumberOfClusters - minNumberOfClusters + 1);
        int rowIndex = 0;
        double[][] data = new double[rows][];
        double[] errors;
        double[] row;
        int avgRPC = ((minRPC + maxRPC) / 2);

        System.out.format("--- minClusters = %3d   maxClusters = %3d   minRPC = %3d   avgRPC = %3d   maxRPC = %3d\n", minNumberOfClusters, maxNumberOfClusters, minRPC, avgRPC, maxRPC);

        for (int i = minNumberOfClusters; i <= maxNumberOfClusters; i++) {

            //create row
            row = new double[COLUMNS];
            row[0] = i;
            errors = getMSEsFromResults(outputAttribute, i, minRPC);
            row[1] = errors[0];
            errors = getMSEsFromResults(outputAttribute, i, avgRPC);
            row[2] = errors[0];
            row[4] = errors[1];
            row[5] = errors[2];
            errors = getMSEsFromResults(outputAttribute, i, maxRPC);
            row[3] = errors[0];

            //write row
            data[rowIndex] = row;
            rowIndex++;
        }

        //SAVE DATA
        saveDataToFile(data, filename);
    }

    /**
     * Find the row in results with minimal MSE
     *
     * @param numberOfClusters Number of clusters
     * @param column           Column of the MSE
     * @return Row index
     */
    private int findMinMSE(int numberOfClusters, int column) {
        int index = -1;
        double minMSE = -1;
        for (int i = 0; i < results.length; i++) {
            if (results[i][CLUSTERS_FIELD] == numberOfClusters) {
                if ((index == -1) || (results[i][column] < minMSE)) {
                    index = i;
                    minMSE = results[i][column];
                }
            }
        }
        return index;
    }

    /**
     * Creates a datafile for the graph of the Ensemble clustering, Bagging and Boosting comparison
     *
     * @param outputAttribute     Number of output attribute
     * @param minNumberOfClusters Minimum number of clusters
     * @param maxNumberOfClusters Maximum number of clusters
     * @param filename            Output filename
     */
    public void createDataFileECBABOComparison(int outputAttribute, int minNumberOfClusters, int maxNumberOfClusters, String filename) {

        //PREPARE DATA
        int rows = (maxNumberOfClusters - minNumberOfClusters + 1);
        int rowIndex = 0;
        int minIndex;
        int column = INDEPENDENT_VARIABLES + outputAttribute * COMPARED_MODELS;
        int columns = results[0].length;
        double errors[];
        double row[];
        double[][] data = new double[rows][];

        for (int i = minNumberOfClusters; i <= maxNumberOfClusters; i++) {

            minIndex = findMinMSE(i, column);
            data[rowIndex] = results[minIndex];

            //CALCULATE RMS
            for (int j = INDEPENDENT_VARIABLES; j < columns; j++) {
                data[rowIndex][j] = Math.sqrt(data[rowIndex][j]);
            }

            rowIndex++;
        }

        //SAVE DATA
        //System.out.printf("result = %s%n", Arrays.deepToString(data));
        saveDataToFile(data, filename);
    }

    public double[][] getBa() {
        return ba;
    }

    public double[][] getBo() {
        return bo;
    }

    public double[][][] getEc() {
        return ec;
    }

    public void createDataFileDCR(int minD, int maxD, int minC, int maxC, int minR, int maxR, String filename) {

        //PREPARE DATA
        int rows = (maxD - minD + 1) * 10;
        int rowIndex = 0;
        int columns = 23;
        double row[];
        double[][] data = new double[rows][];

        for (int d = minD; d <= maxD; d++) {

            ba[d][1] = ba[d][2];
            bo[d][1] = bo[d][2];

            for (int c = (minC - 1); c <= maxC; c++) {

                row = new double[columns];
                row[0] = d;
                row[1] = (c == 1) ? 2 : c;
                row[2] = (d + (c - 1) / 10.0);
                row[3] = ba[d][c];
                row[4] = bo[d][c];

                //EC - SR
                for (int r = minR; r <= maxR; r++) {
                    ec[d][1][r] = ec[d][2][r];
                    ecBV[d][1][r] = ecBV[d][2][r];

                    row[r + 3] = ec[d][c][r];
                    row[r + 3 + 9] = ecBV[d][c][r];
                }

                //System.out.format("%3d   %3d    %7.3f\n", d, c, row[2]);

                data[rowIndex] = row;

                rowIndex++;
            }
        }

        //SAVE DATA
        //System.out.printf("result = %s%n", Arrays.deepToString(data));
        saveDataToFile(data, filename);
    }
}

