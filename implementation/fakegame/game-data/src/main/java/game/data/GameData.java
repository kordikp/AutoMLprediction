package game.data;

import java.util.Vector;

public interface GameData {

    public double[] getVector(int index);

    public int getInstanceNumber();

    public double[][] getInputVectors();

    public double[][] getOutputAttrs();

    public int getONumber();

    public int getINumber();

    public Vector<OutputProducer> getInputFeatures();

    //TODO pozustatek ze stareho gamu.. casem se snazit z refaktorovat.
    public void publishVector(int index);

    public double getTargetOutput(int targetVariable);

    public double[] getTargetOutputs();

}
