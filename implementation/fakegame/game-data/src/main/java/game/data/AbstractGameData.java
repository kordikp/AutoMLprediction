package game.data;

import game.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class AbstractGameData implements GameData {

    private List<OutputAttribute> oAttr = new ArrayList<OutputAttribute>();
    private List<InputFactor> iFactor = new ArrayList<InputFactor>();
    private List<Instance> group = new ArrayList<Instance>();
    private Vector<OutputProducer> inputFeatures;

    private double[][] ivectors; //input vectors
    private double[][] oattrs;


    private int iNumber = 0;
    private int oNumber = 0;
    private int groups = 0;

    private MiningType type;

    public MiningType getDataType() {
        if (type == MiningType.ORDER_PREDICTION) return MiningType.CLASSIFICATION;
        else return type;
    }

    public MiningType getDetailedDataType() {
        return type;
    }

    protected void setDataType() {
        if (getONumber() == 0) {
            type = MiningType.CLUSTERING;
        } else if (getONumber() == 1) {
            type = MiningType.REGRESSION;
        } else {
            for (int i = 0; i < oattrs[0].length; i++) {
                if (oattrs[0][i] > 1) {
                    type = MiningType.ORDER_PREDICTION;
                    return;
                }
            }
            type = MiningType.CLASSIFICATION;
        }
    }

    protected void createOutputAttribute(String name, double max, double min, boolean cont, int sign) {
        boolean out = false;
        name = Utils.removeSpaces(name);
        if (name != null) {
            for (int i = 0; i < oAttr.size(); i++) {
                if (oAttr.get(i).getName().compareTo(name) == 0) { //the same name
                    oAttr.get(i).modifyValues(max, min, cont, sign);
                    out = true;
                    break;
                }
            }
            if (!out) { //new attribute
                oAttr.add(new OutputAttribute(name, max, min, cont, sign));
                oNumber++;
            }
        }
    }

    protected void createInputFactor(String name, double max, double min, double med, boolean cont) {
        boolean out = false;
        name = Utils.removeSpaces(name);
        if (name != null) {
            for (int i = 0; i < iNumber; i++) {
                if (iFactor.get(i).getName().compareTo(name) == 0) { //the same name
                    iFactor.get(i).modifyValues(max, min, med, cont);
                    out = true;
                    break;
                }
            }
            if (!out) { //new factor
                iFactor.add(new InputFactor(name, max, min, med, cont));
                iNumber++;
            }
        }
    }

    /**
     * Gets numner of instances
     *
     * @return Number of instances
     */
    public int getGroups() {
        return groups;
    }

    /**
     * List of instances
     *
     * @return
     */
    public List<Instance> getGroup() {
        return group;
    }

    /**
     * Lists of input attributes properties
     *
     * @return vector of input atributes properties
     */
    public List<InputFactor> getIFactor() {
        return iFactor;
    }

    /**
     * List of output attributes
     *
     * @return
     */
    public List<OutputAttribute> getOAttr() {
        return oAttr;
    }

    /**
     * Gets num. of outputs
     *
     * @return Returns number of outputs
     */
    @Override
    public int getONumber() {
        return this.oNumber;
    }

    /**
     * Get number of input variables
     *
     * @return Returns number of inputs
     */
    @Override
    public int getINumber() {
        return iNumber;
    }

    public void deleteInputFactor(String name) {
        //TODO otestetovat.. prepsano z weka fast vectoru.
        for (int i = 0; i < iNumber; i++) {
            if (iFactor.get(i).getName().compareTo(name) == 0) { //the same name
                iFactor.remove(i);
                iNumber--;
                break;
            }
        }
    }

    @Override
    public double[] getVector(int index) {
        return ivectors[index];
    }

    /**
     * to create new input - output vector (group)
     *
     * @param iname
     * @param ivect
     * @param ovect
     */
    public void setInstance(String iname, double[] ivect, double[] ovect) {
        group.add(new Instance(iname, ivect, ovect));
        groups++;
    }

    @Override
    public int getInstanceNumber() {
        return getGroups();
    }

    public void refreshDataVectors() {
        ivectors = new double[getGroups()][getINumber()]; //Creates space for input game.data
        oattrs = new double[getGroups()][getONumber()];
        for (int i = 0; i < getGroups(); i++) {
            for (int j = 0; j < getINumber(); j++) {
                ivectors[i][j] = group.get(i).getiVal(j);
            }
        }
        for (int i = 0; i < getGroups(); i++) {
            for (int j = 0; j < getONumber(); j++) {
                oattrs[i][j] = group.get(i).getoVal(j);
            }
        }

        setDataType();
    }

    @Override
    public double[][] getInputVectors() {
        return ivectors;
    }

    @Override
    public double[][] getOutputAttrs() {
        return oattrs;
    }

    @Override
    public Vector<OutputProducer> getInputFeatures() {
        if (inputFeatures == null) refreshInputFeatures();
        return inputFeatures;
    }

    //TODO.. tady proverit ze to bude fungovat dobre, pak to dat protected, private.
    public void refreshInputFeatures() {
        Vector<OutputProducer> features = new Vector<OutputProducer>();
        int inputNumber = getINumber();
        for (int i = 0; i < inputNumber; i++)
            features.add(getIFactor().get(i));
        inputFeatures = features;
    }

    public double[] getInputVectorReference(int index) {
        return ivectors[index];
    }

    public double[] getOutputVectorReference(int index) {
        return oattrs[index];
    }

    //na vystup dat da aktualni vektor.. TODO.. tak tohle je az zbytecne slozite.
    public void publishVector(int index) {
        int i = 0;
        for (OutputProducer inputFeature : inputFeatures) {
            ((InputFactor) inputFeature).setValue(group.get(index).getiVal(i++));
        }
        for (int j = 0; j < getONumber(); j++) {
            oAttr.get(j).setValue(group.get(index).getoVal(j));

        }
    }

    public double getTargetOutput(int targetVariable) {
        return oAttr.get(targetVariable).getValue();
    }

    public double[] getTargetOutputs() {
        double[] ovect = new double[getONumber()];
        for (int j = 0; j < getONumber(); j++) ovect[j] = oAttr.get(j).getValue();
        return ovect;
    }

}
