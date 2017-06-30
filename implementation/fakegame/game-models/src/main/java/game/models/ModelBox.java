package game.models;

import java.util.ArrayList;
import java.util.List;

public class ModelBox extends AbstractBox {

    private List<ConnectableModel> models;

    public ModelBox() {
        models = new ArrayList<ConnectableModel>();
    }

    public double[] getOutput(double[] input) {
        int size = models.size();
        double[] d = new double[size];
        for (int i = 0; i < size; i++) {
            d[i] = models.get(i).getOutput(input);
        }
        return d;
    }

    public void addModel(ConnectableModel model) {
        models.add(model);
    }

    public int size() {
        return models.size();
    }

    public ConnectableModel get(int position) {
        return models.get(position);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ConnectableModel cModel : models) {
            sb.append(cModel.toEquation());
            sb.append("\n");
        }
        return sb.toString();
    }
}
