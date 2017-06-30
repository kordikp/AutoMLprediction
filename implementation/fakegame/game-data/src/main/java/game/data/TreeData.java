/**
 * @author Pavel Kordik
 * @version 0.90
 */

package game.data;

import java.util.ArrayList;


/**
 * sa
 * Data object - works with input-output data
 */
@Deprecated
public class TreeData {

    public final static int MAX_INPUTS = 2000;
    public final static int MAX_OUTPUTS = 100;
    public final static int MAX_INSTANCES = 5000;
    public final static int MAX_INSTANCES_FOR_LEARNING = 5000;
    public final static int MAX_CHARS_IN_NAME = 10;
    protected final static int COLORS_DEFINED = MAX_INPUTS;
    protected final static int MAX_NETWORKS = 5 * MAX_OUTPUTS;
    public final static int MAX_GRAPH_RESULUTION = 200;
    public final static int MIN_GRAPH_RESULUTION = 5;
    public final static int MAX_RMS_RECORDS = 1000;
    public final static String IFID = "Input_factor_name";
    protected final static String OAID = "Attribute_name";
    protected final static String GID = "Group_name";
    protected final static String CONTINUOUS = "continuous";
    protected final static String DISTRIBUTE = "distribute";
    protected final static String POSITIVE = "positive";
    protected final static String NEGATIVE = "negative";
    protected final static String NETID = "netid";
    public final static String DEFAULT_FILE_NAME = "simul";
    public final static String DEFAULT_PMML_FILE_NAME = "simul.pmml";
    public final static String DEFAULT_NETWORK_NAME = "newnet";
    public final static int SIMPLE_ENSEMBLE = 0;
    public final static int WEIGHTED_ENSEMBLE = 1;
    private static final int UNIT_LOADER_TYPE_NEURON = 0x01;
    private static final int UNIT_LOADER_TYPE_OPTIMIZATION = 0x02;
    private static final int UNIT_LOADER_TYPE_VIZUALIZATION = 0x03;
    public static final int UNIT_LOADER_TYPE_PREPROCESSING = 0x04;

    public enum funkce {
        AVERAGE, MULTIPLY, MAJORITY
    }

    /*public enum UnitLoaderType {
        Neuron, Optimization, Visualisation, Preprocessing
    } */

    private String autosave_filename = "autosave";
    private String autosave_dir = "";
    private String urlOfPmmlSchema = "";  // url to PMML schema

    private int iNumber = 0;
    private int oNumber = 0;
    private int groups = 0;
    static int pubColor = 0;

    public ArrayList iFactor = new ArrayList();
    public ArrayList oAttr = new ArrayList();
    public ArrayList group = new ArrayList();
    //private GMDHtree boss;

    //RMSData rms;
    //RMSData[] rms;
    //public UnitLoader ul;


    boolean multiNet;

    public int[] vpos;
    int lastRMS = 0;
    int maxSign;
    //private double caccuracy;             preneseno do ModelEvaluation
    //public GMDHnetwork[] net = new GMDHnetwork[MAX_NETWORKS];
    //public GMDHnetwork gnet; // pointer to the network for graph display
    //Color[] color = new Color[COLORS_DEFINED];
    //Color[] prop;


    private int[] vectorToSave;
    private int vectors;


    /**
     * Get number of input variables
     *
     * @return Returns number of inputs
     */
    public int getINumber() {
        return iNumber;
    }

    /**
     * Gets num. of outputs
     *
     * @return Returns number of outputs
     */
    public int getONumber() {
        return this.oNumber;
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
     * Lists of input attributes properties
     *
     * @return vector of input atributes properties
     */
    public ArrayList getIFactor() {
        return iFactor;
    }

    public void setIFactor(ArrayList iFactor) {
        this.iFactor = iFactor;
    }

    /**
     * List of output attributes
     *
     * @return
     */
    public ArrayList getOAttr() {
        return oAttr;
    }

    public void setOAttr(ArrayList oAttr) {
        this.oAttr = oAttr;
    }

    public ArrayList getGroup() {
        return group;
    }

    public void setGroup(ArrayList group) {
        this.group = group;
    }

    /**
     * Training and testing set splitting used by
     *
     * @return
     */
    public int[] getVectorToSave() {
        return vectorToSave;
    }

    public void setVectorToSave(int[] vectorToSave) {
        this.vectorToSave = vectorToSave;
    }

    public void setVectorToSave(int i, int k) {
        this.vectorToSave[i] = k;
    }

    public int getVectors() {
        return vectors;
    }

    public void setVectors(int vectors) {
        this.vectors = vectors;
    }

    //This specifies list of "units" names you want to recieve from UnitLoader by function getUnitsList()
    //This is a little cleaner way to obtain list of "plugins" (or how to call it) than by exporting pointer to UnilLoader class
    //If you call getUnitList with one of these you will recieve list of class names

    public TreeData() {

    }


    /**
     * to create new input - calls InputFactor constructor
     *
     * @param name
     * @param name
     * @param max
     * @param max
     * @param min
     * @param min
     * @param med
     * @param cont
     * @param med
     * @param cont
     */

    public void createInputFactor(String name, double max, double min, double med, boolean cont) {
        boolean out = false;
        // name = Utils.removeSpaces(name);
        if (name != null) {
            for (int i = 0; i < iNumber; i++) {
                if (((InputFactor) iFactor.get(i)).getName().compareTo(name) == 0) { //the same name
                    ((InputFactor) iFactor.get(i)).modifyValues(max, min, med, cont);
                    out = true;
                    break;
                }
            }
            if (!out) { //new factor
                // if (iNumber < MAX_INPUTS) {
                //  iFactor.addElement(new InputFactor(name, max, min, med, cont, Colors.color[(pubColor++ % COLORS_DEFINED)]));
                iNumber++;
                // }
            }
        }
    }


    public void deleteInputFactor(String name) {
        for (int i = 0; i < iNumber; i++) {
            if (((InputFactor) iFactor.get(i)).getName().compareTo(name) == 0) { //the same name
                //System.arraycopy(iFactor, i + 1, iFactor, i, iNumber - i);
                iFactor.remove(iFactor.indexOf(iFactor.get(iFactor.size() - 1)));
                iNumber--;
                break;
            }
        }
    }


    /**
     * to create new output - calls OutputAttribute constructor
     *
     * @param name
     * @param name
     * @param max
     * @param max
     * @param min
     * @param min
     * @param cont
     * @param sign
     * @param cont
     * @param sign
     */

    public void createOutputAttribute(String name, double max, double min, boolean cont, int sign) {
        boolean out = false;
        //   name = Utils.removeSpaces(name);
        if (name != null) {
            for (int i = 0; i < oAttr.size(); i++) {
                if (((OutputAttribute) oAttr.get(i)).getName().compareTo(name) == 0) { //the same name
                    ((OutputAttribute) oAttr.get(i)).modifyValues(max, min, cont, sign);
                    out = true;
                    break;
                }
            }
            if (!out) { //new attribute
                //   if (oNumber < MAX_OUTPUTS) {
                oAttr.add(new OutputAttribute(name, max, min, cont, sign));
                oNumber++;
                //  }
            }
        }
    }


    public void deleteOutputAttribute(String name) {
        for (int i = 0; i < oNumber; i++) {
            if (((OutputAttribute) oAttr.get(i)).getName().compareTo(name) == 0) { //the same name
                iFactor.remove(oAttr.indexOf(oAttr.get(oAttr.size() - 1)));
                oNumber--;
                break;
            }
        }
    }


    /**
     * to create new input - output vector (group)
     *
     * @param iname
     * @param iname
     * @param ivect
     * @param ovect
     * @param ivect
     * @param ovect
     */
    public void newInstance(String iname, double[] ivect, double[] ovect) {
        //    group.addElement(new Instance(this, iname, ivect, ovect));
        groups++;
    }

    public boolean newInstance(String iname) {
        //    iname = Utils.removeSpaces(iname);
        if (iname == null) {
            return false;
        }
        if (oNumber < 1) {
            return false;
        }
        if (iNumber < 1) {
            return false;
        }
/*
        Was here ot ensure that every instance will have unque name.
        This is obsolete now.
        TODO: Implement using hash map. Or consider removing completely.
        for (int i = 0; i < groups; i++) {
            if (((Instance) group.elementAt(i)).getName().compareTo(iname) == 0) { //the same name
                ((Instance) group.elementAt(i)).modifyValues();
                return (false);
            }
        }*/
        //new instance
        // if (groups < MAX_INSTANCES) {
        //group.addElement(new Instance(iname));
        groups++;
        //}
        return (true);
    }

    public void wipeTreeData() {
        iFactor = new ArrayList();
        oAttr = new ArrayList();
        group = new ArrayList();
        groups = 0;
        iNumber = 0;
        oNumber = 0;
        pubColor = 0;
//        System.gc();
    }

    /**
     * looks for the group with the specified name
     *
     * @param name
     * @param name
     */

    public int findGroup(String name) {
        for (int i = 0; i < groups; i++) {
            if (((Instance) group.get(i)).getName().compareTo(name) == 0) { //the same name
                return i;
            }
        }
        return -1; //search failed
    }


    /**
     * returns the Euclid distance of two vectors
     *
     * @param u
     * @param v
     * @param u
     * @param v
     */

    double getHyperDistance(double[] u, double[] v) {
        double d = 0;
        double r = 0;
        for (int j = 0; j < iNumber; j++) {
            r = (u[j] - v[j]) * (u[j] - v[j]);
            d += r;
        }
        return d;
    }


    /**
     * not implemented yet
     */

    public void setMedian() {
    }


    /**
     * not implemented yet
     */

    public void countRanges() {
    }


    /**
     * not implemented yet
     *
     * @param sourceColumn
     * @param sourceColumn
     * @param time
     * @param time
     * @param minus
     * @param minus
     * @param res
     * @param frget
     * @param res
     * @param frget
     */

    public void dataFilterToNewColumn(int sourceColumn, int time, boolean minus, boolean res, boolean frget) {
    }


    /**
     * not implemented yet
     *
     * @param sourceColumn
     * @param sourceColumn
     * @param targetColumn
     * @param targetColumn
     * @param time
     * @param time
     * @param minus
     * @param minus
     * @param res
     * @param frget
     * @param res
     * @param frget
     */

    public void dataFilter(int sourceColumn, int targetColumn, int time, boolean minus, boolean res, boolean frget) {
    }


    //TODO: provizorni meroda, zjistit, kde je jiz oNumber konstantni a tam inicializaci dat, pak metodu odstranit
    public void initVpos() {
        vpos = new int[oNumber];
    }

    public boolean isOutputBinary(int output_index) {
        return vpos[output_index] >= 0;
    }


    public int getOutputNum(int output_index) {
        return vpos[output_index];
    }

    public void computeBinaryOutputs(int output_index) {
        vpos[output_index] = 0;
        for (int i = 0; i < groups; i++) {
            if (((Instance) group.get(i)).getoVal(output_index) == 1) {
                vpos[output_index]++;
            } else if (((Instance) group.get(i)).getoVal(output_index) != 0) {
                vpos[output_index] = Integer.MIN_VALUE;
                break;
            }
        }
    }

    /*
     returns true when all output variables are binary
     */
    public boolean allOutputsBinary() {
        double val;
        for (int i = 0; i < groups; i++) {
            for (int k = 0; k < oNumber; k++) {
                val = ((Instance) group.get(i)).getoVal(k);
                if ((val != 0) && (val != 1)) return false;
            }
        }
        return true;
    }

    /**
     * this method is used for purposes of GlobalData method publish data to maintain encapsulation
     * turns this instance of shallow copy of myData
     *
     * @param myData the object we want to make shallow copy of
     */
    protected void loadData(TreeData myData) {
        oNumber = myData.getONumber(); //Number of network outputs
        iNumber = myData.getINumber(); //Number of network inputs
        groups = myData.getGroups(); //Number of instances
        group = myData.group;
        iFactor = myData.iFactor;
        oAttr = myData.oAttr; // point to the same data vectors
    }


    /**
     * Checks if the data stored in GlobalData are classification problem. If there is only one output,
     * we pressume it is regression, if there are more, it is classification, providing that the outputs are
     * binary and one and only one output is 1.
     *
     * @return Returns true for classification, false for regression.
     */
    public boolean usesClassification() {
        double val;
        boolean singleOutput;       //if true, the Attribute has exactly one output with value = 1

        if (oNumber == 1)
            return false;
        for (int i = 0; i < groups; i++) {
            singleOutput = false;
            for (int k = 0; k < oNumber; k++) {
                val = ((Instance) group.get(i)).getoVal(k);
                if ((val != 0) && (val != 1))
                    return false;         //output attribute of Instance is not binary, it is regression
                if (val == 1) {
                    if (singleOutput)
                        return false;     //there are two output attributes with value == 1, it is regression
                    singleOutput = true;
                }
            }
            if (!singleOutput)
                return false;    //there is no output attribute with value == 1, it is regression
        }
        return true;
    }
}


