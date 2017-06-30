/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.configuration;


//import java.io.Serializable;

/**
 * Configuration is the class for passing and keeping the GMDHnetwork configuration
 */
public class NetworkConfiguration implements java.io.Serializable {
    final static int MAX_NEURONS_IN_A_LAYER = 30;
    public final static int MAX_LAYERS = 20;
    final static int MAX_COEFS = 5;
    public final static int LEARNING_RECORDS = 50;

    public void setBuildWhileDec(boolean buildWhileDec) {
        this.buildWhileDec = buildWhileDec;
    }

    /**
     * inputs just from previous layers
     */

    public final static int YOUNG = 0;
    /**
     * more inputs to neurons from near previous layers
     */
    public final static int YOUNGER = 1;
    /**
     * random inputs
     */
    public final static int MIDDLE = 2;
    /**
     * more inputs to neurons from first layers
     */
    public final static int OLDER = 3;
    /**
     * inputs just from first layers
     */
    public final static int OLD = 4;

    private int criterion; // criterion function type
    private String selectionCriterion;
    private int vectorsInTestingSet;
    private boolean justTwo;
    private boolean commonResponse;
    private boolean buildWhileDec;

    public String getSelectionCriterion() {
        return selectionCriterion;
    }

    public void setSelectionCriterion(String selectionCriterion) {
        this.selectionCriterion = selectionCriterion;
    }

    public boolean isJustTwo() {
        return justTwo;
    }

    public void setJustTwo(boolean justTwo) {
        this.justTwo = justTwo;
    }

    public boolean isCommonResponse() {
        return commonResponse;
    }

    public void setCommonResponse(boolean commonResponse) {
        this.commonResponse = commonResponse;
    }

    public boolean isNormalization() {
        return normalization;
    }

    public void setNormalization(boolean normalization) {
        this.normalization = normalization;
    }

    public int getParents() {
        return parents;
    }

    public void setParents(int parents) {
        this.parents = parents;
    }

    public int[] getLayerNNum() {
        return layerNNum;
    }

    public void setLayerNNum(int[] layerNNum) {
        this.layerNNum = layerNNum;
    }

    public int[] getLayerINum() {
        return layerINum;
    }

    public void setLayerINum(int[] layerINum) {
        this.layerINum = layerINum;
    }

    public boolean[] getInCrit() {
        return inCrit;
    }

    public void setInCrit(boolean[] inCrit) {
        this.inCrit = inCrit;
    }

    public boolean[] getTypeAllowed() {
        return typeAllowed;
    }

    public void setTypeAllowed(boolean[] typeAllowed) {
        this.typeAllowed = typeAllowed;
    }

    private boolean normalization;
    private int parents;
    private int[] layerNNum = new int[MAX_LAYERS];
    private int[] layerINum = new int[MAX_LAYERS];
    //TODO konstanta z TreeDat byla nahrazena.. 
    private boolean[] inCrit = new boolean[300];
    private boolean[] typeAllowed = new boolean[1];

    /**
     * default configuration of the GMDH network
     */
    public NetworkConfiguration() {
        justTwo = false; //neurons have just two inputs in a layer
        commonResponse = false; //weighted average of response/just the best neuron in a layer responses
        parents = OLDER;
        normalization = true;
        buildWhileDec = false; //true;
        //setDefaultLayerNeuronsNumbers()
        layerNNum[0] = 5;
        layerNNum[1] = 6;
        layerNNum[2] = 5;
        layerNNum[3] = 5;
        layerNNum[4] = 4;
        layerNNum[5] = 4;
        layerNNum[6] = 4;
        layerNNum[7] = 4;
        layerNNum[8] = 4;
        layerNNum[9] = 3;
        layerNNum[10] = 3;
        layerNNum[11] = 3;
        layerNNum[12] = 3;
        layerNNum[13] = 3;
        layerNNum[14] = 3;
        layerNNum[15] = 3;
        layerNNum[16] = 3;
        layerNNum[17] = 3;
        layerNNum[18] = 2;
        layerNNum[19] = 2;

        //setDefaultLayerInitialNeuronsNumbers()
        layerINum[0] = 30;
        layerINum[1] = 40;
        layerINum[2] = 45;
        layerINum[3] = 40;
        layerINum[4] = 35;
        layerINum[5] = 35;
        layerINum[6] = 35;
        layerINum[7] = 35;
        layerINum[8] = 35;
        layerINum[9] = 35;
        layerINum[10] = 35;
        layerINum[11] = 35;
        layerINum[12] = 35;
        layerINum[13] = 35;
        layerINum[14] = 35;
        layerINum[15] = 35;
        layerINum[16] = 35;
        layerINum[17] = 30;
        layerINum[18] = 25;
        layerINum[19] = 25;
        for (int i = 0; i < 300; i++) {
            inCrit[i] = false;
        }
        // for (int i = 0; i < UnitLoader.MAX_CLASSES; i++) {
        //     typeAllowed[i] = true;
        // }
        vectorsInTestingSet = 30;
    }

    /**
     * ask if the type of the GMDH unit is enabled for the GMDH generation process
     *
     * @param which
     */
    public boolean neuronTypeAllowed(int which) {
        return typeAllowed[which];
    }

    /**
     * returns the number of surviving GMDH units in the layer
     *
     * @param layerNum
     */
    public int getLayerNeuronsNumber(int layerNum) {
        return layerNNum[layerNum];
    }

    /**
     * sets the number of surviving GMDH units in the layer
     *
     * @param layerNum
     * @param num
     */
    public void setLayerNeuronsNumber(int layerNum, int num) {
        layerNNum[layerNum] = num;
    }

    /**
     * gets the initial population size in the GMDH layer
     *
     * @param layerNum
     */
    public int getLayerInitialNeuronsNumber(int layerNum) {
        return layerINum[layerNum];
    }

    /**
     * sets the initial population size in the GMDH layer
     *
     * @param layerNum
     * @param num
     */
    public void setLayerInitialNeuronsNumber(int layerNum, int num) {
        layerINum[layerNum] = num;
    }

    /**
     * this is used to correct values inserted by the user
     */
    public int getMaxNeuronsInLayer() {
        int max = 0;
        for (int i = 0; i < MAX_LAYERS; i++) {
            max = (max < getLayerNeuronsNumber(i)) ? getLayerNeuronsNumber(i) : max;
        }
        return max;
    }

    /**
     * criterion = number of the output attribute modelled by the GMDH network
     *
     * @param crit
     */
    public void setCriterion(int crit) {
        criterion = crit;
    }

    /**
     * which models are going to be created
     *
     * @param chbox
     */
    public void setCriterion(boolean[] chbox) {
        criterion = 300;
        System.arraycopy(chbox, 0, inCrit, 0, 300);
    }

    /**
     * criterion = number of the output attribute modelled by the GMDH network
     */
    public int getCriterion() {
        return criterion;
    }

    public int getVectorsInTestingSet() {
        return vectorsInTestingSet;
    }

    public int getVectorsInLearningSet() {
        return vectorsInTestingSet;
    }

    public void setVectorsInTestingSet(int vect) {
        vectorsInTestingSet = vect;
    }

    /**
     * to clone instances of this class
     */
    public NetworkConfiguration cloneConfiguration() {
        NetworkConfiguration c = new NetworkConfiguration();
        c.criterion = criterion;
        c.vectorsInTestingSet = vectorsInTestingSet;
        c.justTwo = justTwo;
        c.commonResponse = commonResponse;
        c.parents = parents;
        c.normalization = normalization;
        c.buildWhileDec = this.buildWhileDec;
        System.arraycopy(layerNNum, 0, c.layerNNum, 0, NetworkConfiguration.MAX_LAYERS);
        System.arraycopy(layerINum, 0, c.layerINum, 0, NetworkConfiguration.MAX_LAYERS);
        System.arraycopy(inCrit, 0, c.inCrit, 0, 300);
        //System.arraycopy(typeAllowed, 0, c.typeAllowed, 0, UnitLoader.MAX_CLASSES);
        return c;
    }

    /**
     * isBuildWhileDec
     *
     * @return boolean
     */
    public boolean isBuildWhileDec() {
        return buildWhileDec;
    }

    public void setTypeAllowed(int i, boolean selected) {
        typeAllowed[i] = selected;

    }
}
