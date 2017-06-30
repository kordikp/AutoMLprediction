package game.data;

import java.util.Vector;

/**
 * This is class is singleton - it provides the global access to data shared by all fakegame components
 */
@Deprecated
public class GlobalData extends TreeData {
    private double[][] ivectors; //input vectors
    private double[][] oattrs; //output vectors
    private boolean metadata = false; // enable, when toEquation() of metamodels should return predefined string
    private Vector<OutputProducer> inputFeatures;

    private Vector<String> inputString;  // a string predefined for metamodels

    public double[][] getIvectors() {
        return ivectors;
    }

    public double[][] getOattrs() {
        return oattrs;
    }

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public Vector<String> getInputString() {
        return inputString;
    }

    public void setInputString(Vector<String> inputString) {
        this.inputString = inputString;
    }

    public void setInputString(int index, String inputString) {
        this.inputString.set(index, inputString);
    }

    public int getInstNumber() {
        return getGroups();
    }

    public int decInstNumber() {
        return (super.getGroups() - 1);
    }

    private static GlobalData uniqueInstance;

    private GlobalData() {
    }

    public static synchronized GlobalData getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new GlobalData();
        }
        return uniqueInstance;
    }

    /**
     * This function makes available training data to be shared by all models
     *
     * @param myData The data to be published
     */
    public void publishData(TreeData myData) {
        super.loadData(myData);
        refreshDataVectors();
        refreshInputFeatures();
    }

    public Vector<OutputProducer> getInputFeatures() {
        if (inputFeatures == null) refreshInputFeatures();
        return inputFeatures;
    }

    public void refreshInputFeatures() {
        Vector<OutputProducer> features = new Vector<OutputProducer>();
        int inputNumber = GlobalData.getInstance().getINumber();
        for (int i = 0; i < inputNumber; i++)
            features.add((OutputProducer) getIFactor().get(i));
        inputFeatures = features;
    }

    public void refreshDataVectors() {
        ivectors = new double[super.getINumber()][getGroups()]; //Creates space for input game.data
        oattrs = new double[super.getONumber()][getGroups()];
        for (int j = 0; j < getGroups(); j++) {
            for (int i = 0; i < super.getINumber(); i++) {
                //              if (normalization) {         //TODO configure normalization
                ivectors[i][j] = ((Instance) group.get(j)).getiVal(i);
                /*               } else {
                                   ivectors[j][i] = ((Instance) myData.instNumber.elementAt(j)).getiVal(i);
                            }
                */
            }
        }
        for (int j = 0; j < getGroups(); j++) {
            for (int i = 0; i < super.getONumber(); i++) {
                //               if (normalization) {
                oattrs[i][j] = ((Instance) group.get(j)).getoVal(i);
                /*               } else {
                                    oattrs[j][i] = ((Instance) myData.instNumber.elementAt(j)).getoVal(i);
                                }
                           }
                */
            }
        }
    }

    /**
     * transforms data vector into normalized form in (0,1) range
     *
     * @param input_vector data vector
     * @return normalized data vector
     */
    public double[] normalizeInputVector(double[] input_vector) {
        double[] normalized_inputVector = new double[super.getINumber()];
        for (int i = 0; i < super.getINumber(); i++) {
            normalized_inputVector[i] = ((InputFactor) iFactor.get(i)).getStandardValue(input_vector[i]);
        }
        return normalized_inputVector;
    }

    /**
     * decodes normalized input vector
     *
     * @param normalized_input_vector vector to decode (0,1)
     * @return data vector
     */
    public double[] denormalizeInputVector(double[] normalized_input_vector) {
        double[] inputVector = new double[super.getINumber()];
        for (int i = 0; i < super.getINumber(); i++) {
            inputVector[i] = ((InputFactor) iFactor.get(i)).decodeStandardValue(normalized_input_vector[i]);
        }
        return inputVector;
    }

    public double getStandardOutput(int targetVariable, double outValue) {
        return ((OutputAttribute) oAttr.get(targetVariable)).getStandardValue(outValue);
    }

    public double decodeStandardOutput(int targetVariable, double normalizedOutput) {
        return ((OutputAttribute) oAttr.get(targetVariable)).decodeStandardValue(normalizedOutput);
    }

    /**
     * Returns the Name of ith input feature, in case of metamodels inputString has to be set in advance
     *
     * @param i index of the feature
     * @return name of ith input feature or predefined inputString in case of metamodels
     */
    public String getInputName(int i) {
        if (metadata) return inputString.get(i);
        return ((InputFactor) iFactor.get(i)).getName();
    }

    public double getTargetOutput(int targetVariable) {
        return ((OutputAttribute) oAttr.get(targetVariable)).getValue();
    }

    public double[] getTargetOutputs() {
        double[] ovect = new double[getONumber()];
        for (int j = 0; j < getONumber(); j++) ovect[j] = ((OutputAttribute) oAttr.get(j)).getValue();
        return ovect;
    }

    public void publishVector(int index) {
        int i = 0;
        for (OutputProducer inputFeature : inputFeatures) {
            ((InputFactor) inputFeature).setValue(((Instance) group.get(index)).getiVal(i++));
        }
        for (int j = 0; j < getONumber(); j++) {
            ((OutputAttribute) oAttr.get(j)).setValue(((Instance) group.get(index)).getoVal(j));

        }
    }
}