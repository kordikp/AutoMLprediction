/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Administrator
 */
public class SpiralVisualisation extends Frame {
    private SpiralImage image;
    private double[][] spiralDatas;

    public SpiralVisualisation(TrainingSet trainingSet) throws Exception {
        /*
        spiralDatas = this.transformInputs(trainingSet);
        int [][] scaledInputs = this.scaleInputs(spiralDatas, 400, 2);
        this.setSize(800, 800);
        this.image = new SpiralImage(scaledInputs);
        this.add(image,BorderLayout.CENTER);
         * */
        SpiralProjection projection = new SpiralProjection();
        this.add(projection, BorderLayout.CENTER);
        this.setSize(800, 800);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });
    }

    public SpiralVisualisation() {
        this.image = new SpiralImage();
        this.add(image, BorderLayout.CENTER);
        this.setSize(800, 800);
    }

    private int retype(double x) {
        if (x >= 0 && x < 0.01) return 0;
        if (x >= 0.99 && x < 1.01) return 1;
        return 2;
    }

    private int[][] scaleInputs(double[][] inputs, int scaleFactor, int inputsNumber) throws Exception {
        int[][] scaledInputs = new int[inputs.length][];
        for (int i = 0; i < inputs.length; i++) {
            scaledInputs[i] = new int[inputs[i].length];
            for (int k = 0; k < inputsNumber; k++) {
                scaledInputs[i][k] = (int) (inputs[i][k] * scaleFactor);
            }
            for (int k = inputsNumber; k < inputs[i].length; k++) {
                scaledInputs[i][k] = this.retype(inputs[i][k]);
            }

        }
        return scaledInputs;
    }

    public double[][] transformInputs(TrainingSet trainingSet) throws Exception {
        double[][] inputs = new double[trainingSet.size()][];
        int inputsNumber = trainingSet.getTrainingPattern(0).inputsNumber();
        int outputsNumber = trainingSet.getTrainingPattern(0).desiredOutputsNumber();
        for (int i = 0; i < inputs.length; i++) {
            TrainingPattern pattern = trainingSet.getTrainingPattern(i);
            inputs[i] = new double[inputsNumber + outputsNumber];
            for (int k = 0; k < inputsNumber; k++) {
                inputs[i][k] = pattern.getInput(k);
            }
            for (int k = 0; k < outputsNumber; k++) {
                inputs[i][k + inputsNumber] = pattern.getDesiredOutput(k);
            }
        }
        return inputs;
    }


    public void setType(double[] types) {
        for (int i = 0; i < types.length; i++) {
            this.image.setType(i, this.retype(types[i]));
        }
    }

    public void repaintSpirals() {
        this.image.repaint();
    }


}
