package game.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Class for reading arff files
 */
public class ArffGameData extends AbstractGameData {

    public ArffGameData(String fileName) {
        readArffFile(fileName);
    }

    private void readArffFile(String fileName) {
        try {
            BufferedReader sourceFile = new BufferedReader(new FileReader(fileName));
            String line;
            String[] data;
            double[] inputs;
            double[] outputs;
            int instanceNumber = 0;
            Hashtable<String, Integer> outputMap = new Hashtable<String, Integer>();
            //read meta data part of the file
            while ((line = sourceFile.readLine()) != null) {
                line = line.toLowerCase();
                line = line.trim();
                if (line.equals("") || line.startsWith("%")) continue;
                else if (line.startsWith("@attribute")) {
                    data = line.split("\\s++", 3);
                    data[1] = data[1].replace("'", "");
                    if (!data[1].equals("class")) {
                        createInputFactor(data[1], 0, 0, 0, true);
                    } else {
                        data[2] = data[2].replaceAll("\\s++", "");
                        data[2] = data[2].substring(1, data[2].length() - 1);
                        data = data[2].split(",");
                        for (int i = 0; i < data.length; i++) {
                            createOutputAttribute(data[i], 0, 0, true, 0);
                            outputMap.put(data[i], i);
                        }
                    }
                } else if (!line.startsWith("@")) {  //DATA
                    data = line.split(",");
                    //inputs
                    inputs = new double[data.length - 1];
                    for (int i = 0; i < inputs.length; i++) {
                        data[i] = data[i].replace("?", "0");
                        inputs[i] = Double.parseDouble(data[i]);
                    }
                    //outputs
                    outputs = new double[outputMap.size()];
                    data[data.length - 1] = data[data.length - 1].replaceAll("\\s++", "");
                    outputs[outputMap.get(data[data.length - 1])] = 1;

                    setInstance("g" + instanceNumber, inputs, outputs);
                    instanceNumber++;
                }
            }
            refreshDataVectors();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
