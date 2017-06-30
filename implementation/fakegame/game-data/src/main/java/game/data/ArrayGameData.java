package game.data;

public class ArrayGameData extends AbstractGameData {

    public ArrayGameData(double[][] inputs, double[][] outputs) {

        for (int i = 0; i < inputs[0].length; i++) {
            createInputFactor("i" + i, 0, 0, 0, true);
        }

        for (int i = 0; i < outputs[0].length; i++) {
            createOutputAttribute("o" + i, 0, 0, true, 0);
        }

        for (int i = 0; i < inputs.length; i++) {
            setInstance("g" + i, inputs[i], outputs[i]);
        }

        refreshDataVectors();
    }

    public ArrayGameData() {
    }

    public void createInputFactor(String name, double max, double min, double med, boolean cont) {
        super.createInputFactor(name, max, min, med, cont);
    }

    public void createOutputAttribute(String name, double max, double min, boolean cont, int sign) {
        super.createOutputAttribute(name, max, min, cont, sign);
    }


}
