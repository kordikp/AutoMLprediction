/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Administrator
 */
public class InputFileReader {


    private BufferedReader input;

    public InputFileReader(String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder contents = new StringBuilder();
        input = new BufferedReader(new FileReader(file));


    }

    private String readLine() {
        try {
            return this.input.readLine();

        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    private int extractNumberInputs(String line, String delimiter) throws Exception {
        line.trim();
        String[] strings = line.split(delimiter);
        if (strings.length != 2 || !strings[0].matches("inputs")) throw new Exception("input file corruption");
        else {
            return Integer.valueOf(strings[1]).intValue();
        }
    }

    private int extractNumberOutputs(String line, String delimiter) throws Exception {
        String[] strings = line.split(delimiter);
        if (strings.length != 2 || !strings[0].matches("outputs")) throw new Exception("input file corruption");
        else {
            return Integer.valueOf(strings[1]).intValue();
        }
    }

    private TrainingPattern extractTrainingPattern(String line, String delimiter, int inputsNumber, int outputsNumber) {
        double[] inputs = new double[inputsNumber];
        double[] desiredOutputs = new double[outputsNumber];
        line = line.trim();
        String[] strings = line.split(delimiter);
        String[] stringWithoutSpace = new String[inputsNumber + outputsNumber];
        int index = 0;
        for (int i = 0; i < strings.length; i++) {
            if (!strings[i].matches("")) {
                stringWithoutSpace[index++] = strings[i];
            }
        }
        for (int i = 0; i < inputsNumber; i++) {
            inputs[i] = Double.valueOf(stringWithoutSpace[i]).doubleValue();
        }
        for (int i = 0; i < outputsNumber; i++) {
            desiredOutputs[i] = Double.valueOf(stringWithoutSpace[i + inputsNumber]).doubleValue();
        }
        TrainingPattern pattern = new TrainingPattern(inputs, desiredOutputs);
        return pattern;
    }

    public TrainingSet extractInputFile(String delimiter, boolean normalize) throws Exception {
        String line;
        int index = 0;
        try {
            line = this.readLine();
            int numberInputs = this.extractNumberInputs(line, delimiter);
            line = this.readLine();
            int numberOutputs = this.extractNumberOutputs(line, delimiter);
            TrainingSet trainingSet = new TrainingSet();

            while ((line = this.readLine()) != null) {
                if (index == 40) {
                    System.out.println("afe");
                }
                TrainingPattern pattern = this.extractTrainingPattern(line, delimiter, numberInputs, numberOutputs);
                trainingSet.addTrainingPattern(pattern);
                index++;
            }
            if (normalize) trainingSet = this.nomalize(trainingSet);
            return trainingSet;
        } catch (Exception ex) {
            throw new Exception("InputFileReader: extractInputFile -> " + ex.getMessage() + index);
        }
    }

    private double[][] findExtrems(TrainingSet trainingSet) throws Exception {
        int inputsNumber = trainingSet.getTrainingPattern(0).inputsNumber();
        int outputsNumber = trainingSet.getTrainingPattern(0).desiredOutputsNumber();
        double[][] extrems = new double[inputsNumber + outputsNumber][2];
        for (int i = 0; i < extrems.length; i++) {
            extrems[i][0] = 0;
            extrems[i][1] = 0;
        }
        Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
        while (patternIterator.hasNext()) {
            TrainingPattern pattern = patternIterator.next();
            for (int k = 0; k < inputsNumber; k++) {
                if (extrems[k][0] > pattern.getInput(k))
                    extrems[k][0] = pattern.getInput(k);
                if (extrems[k][1] < pattern.getInput(k))
                    extrems[k][1] = pattern.getInput(k);
            }
            for (int k = 0; k < outputsNumber; k++) {
                if (extrems[inputsNumber + k][0] > pattern.getDesiredOutput(k))
                    extrems[inputsNumber + k][0] = pattern.getDesiredOutput(k);
                if (extrems[inputsNumber + k][1] < pattern.getDesiredOutput(k))
                    extrems[inputsNumber + k][1] = pattern.getDesiredOutput(k);
            }
        }
        return extrems;
    }

    public TrainingSet nomalize(TrainingSet trainingSet) throws Exception {
        double[][] extrems = this.findExtrems(trainingSet);
        for (int i = 0; i < trainingSet.size(); i++) {
            TrainingPattern trainingPattern = trainingSet.getTrainingPattern(i);
            for (int k = 0; k < trainingPattern.inputsNumber(); k++) {
                double normalizedValue = (trainingPattern.getInput(k) - extrems[k][0]) / (extrems[k][1] - extrems[k][0]);
                trainingPattern.setInput(k, normalizedValue);
            }
            for (int k = 0; k < trainingPattern.desiredOutputsNumber(); k++) {
                int index = trainingPattern.inputsNumber() + k;
                double normalizedValue = (trainingPattern.getDesiredOutput(k) - extrems[index][0]) / (extrems[index][1] - extrems[index][0]);
                trainingPattern.setDesiredOutput(k, normalizedValue);
            }
        }
        return trainingSet;
    }

    private void scaleInputs(TrainingSet trainingSet, int scaleFactor) throws Exception {
        Iterator<TrainingPattern> patternIterator = trainingSet.getTraningSet().iterator();
        while (patternIterator.hasNext()) {
            TrainingPattern pattern = patternIterator.next();
            for (int k = 0; k < pattern.inputsNumber(); k++) {
                pattern.setInput(k, pattern.getInput(k) * scaleFactor);
            }
        }
    }

}
