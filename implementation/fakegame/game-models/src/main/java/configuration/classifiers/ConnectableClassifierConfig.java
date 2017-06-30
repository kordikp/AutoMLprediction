package configuration.classifiers;

import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.SelectionSet;

import java.util.Random;

/**
 * Configuration class for connectable classifier.
 */
public class ConnectableClassifierConfig extends ClassifierOperator {
    @Property(name = "Selected inputs", description = "Array, where each item represent if input is allowed or not.")
    @SelectionSet(key = "selectedInputs", type = boolean.class)
    protected boolean[] selectedInputs;

    public ConnectableClassifierConfig(int inputsNumber) {
        config = null;
        selectedInputs = new boolean[inputsNumber];
        for (int i = 0; i < selectedInputs.length; i++) selectedInputs[i] = true;
    }

    protected String variablesToString() {
        String output = "(";
        for (int i = 0; i < selectedInputs.length; i++) {
            if (selectedInputs[i]) output += "1";
            else output += "0";
        }
        output += ")";
        return output;
    }

    private boolean hasAllElementsOfType(boolean[] array, boolean type) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != type) return false;
        }
        return true;
    }

    public ConnectableClassifierConfig clone() {
        ConnectableClassifierConfig newObject;
        newObject = (ConnectableClassifierConfig) super.clone();
        if (selectedInputs != null) {
            newObject.selectedInputs = new boolean[selectedInputs.length];
            for (int i = 0; i < selectedInputs.length; i++) {
                newObject.selectedInputs[i] = selectedInputs[i];
            }
        }
        return newObject;
    }

    public void setSelectedInputs(boolean[] selectedInputs) {
        if (hasAllElementsOfType(selectedInputs, false)) {
            Random rnd = new Random();
            selectedInputs[rnd.nextInt(selectedInputs.length)] = true;
        }
        this.selectedInputs = selectedInputs;
    }

    public boolean[] getSelectedInputs() {
        return selectedInputs;
    }
}
