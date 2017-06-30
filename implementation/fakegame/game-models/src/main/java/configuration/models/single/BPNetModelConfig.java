/**
 * @author Pavel Kordik
 * @version 0.90
 */
package configuration.models.single;


/**
 * Class for the Perceptron unit configuration
 */
public class BPNetModelConfig extends ModelSingleConfigBase {
    /**
     * maximum number of hidden layers in the perceptron network
     */
    private final static int MAX_BP_LAYERS = 5;
    /**
     * randomly generated numbers of neurond in hidden layers of the perceptron network
     */
    private int[] lNum = new int[MAX_BP_LAYERS];
    /**
     * number of epochs (see the Back-Propagation learning process)
     */

    int epoch;
    /**
     * the interval after that is the error shown/recorded/kept
     */
    int showError, recError, keepError;
    /**
     * the bias value and the range for parameters random initialization<br>(learning speed, inertia coefficient, sigmoid senzitivity)
     */
    double lsb, icb, ssb, lsr, icr, ssr;

    /**
     * to initialize parameters (default values)
     */
    public BPNetModelConfig() {
        super();
        //setMaximumComplexityOfBPNeuralNetwork
        lNum[0] = 6;
        lNum[1] = 2;
        lNum[2] = 0;
        lNum[3] = 0;
        lNum[4] = 0;
        epoch = 2000;
        showError = 100;
        recError = 10; //max allowed : epoch/recError == MAX_RMS_RECORD (500)
        keepError = 100;
        lsb = 0.1;
        icb = 0.8;
        ssb = 2;
        lsr = 5;
        icr = 4;
        ssr = 0.5;
    }

    /**
     * to get the number of neurons in hidden layers
     *
     * @param layerNum number of layer
     * @return no. of neurons
     */
    public int getBPNeurons(int layerNum) {
        if (layerNum < MAX_BP_LAYERS) {
            return lNum[layerNum];
        } else {
            return 0;
        }
    }

    /**
     * to set the number of neurons in the hidden layer
     *
     * @param layerNum number of layer
     * @param num      number of neurons
     */
    void setBPNeurons(int layerNum, int num) {
        if (layerNum < MAX_BP_LAYERS) {
            lNum[layerNum] = num;
        }
    }

    public int[] getLNum() {
        return lNum;
    }

    public void setLNum(int[] lNum) {
        this.lNum = lNum;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public int getShowError() {
        return showError;
    }

    public void setShowError(int showError) {
        this.showError = showError;
    }

    public int getRecError() {
        return recError;
    }

    public void setRecError(int recError) {
        this.recError = recError;
    }

    public int getKeepError() {
        return keepError;
    }

    public void setKeepError(int keepError) {
        this.keepError = keepError;
    }

    public double getLsb() {
        return lsb;
    }

    public void setLsb(double lsb) {
        this.lsb = lsb;
    }

    public double getIcb() {
        return icb;
    }

    public void setIcb(double icb) {
        this.icb = icb;
    }

    public double getSsb() {
        return ssb;
    }

    public void setSsb(double ssb) {
        this.ssb = ssb;
    }

    public double getLsr() {
        return lsr;
    }

    public void setLsr(double lsr) {
        this.lsr = lsr;
    }

    public double getIcr() {
        return icr;
    }

    public void setIcr(double icr) {
        this.icr = icr;
    }

    public double getSsr() {
        return ssr;
    }

    public void setSsr(double ssr) {
        this.ssr = ssr;
    }
}
