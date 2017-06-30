/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

/**
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //test.testOr();
        //for (int i = 0; i < 5; i++)
        //test.testHousing("housing.data");
        //System.out.println(sigmoid.calculateDerivative(0.2));
        //test.testXor();
        //test.testSpiral("Spiral.txt");
        /*
        TrainingSet trainingSet;
        int [][]inputs;
        try{
        // TODO code application logic here
        
        //spiral.setVisible(true);
            InputFileReader input = new InputFileReader("spiral.txt");
            trainingSet = input.extractInputFile(" ", false);
            try{
                SpiralVisualisation spiral = new SpiralVisualisation(trainingSet);
                spiral.setVisible(true);
            
            }
            catch(Exception ex){
                System.out.println(ex.toString());
            }

        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        
        System.out.println("afeofjeo");
         *
         *
         * */
        TestNeuralNetwork test = new TestNeuralNetwork();


        try {

            //test.makeSpiralData();
            //test.showSpiral();
            test.testXor2();
            //NeuralNetwork network = new NeuralNetwork(2,1,1, new ActivationFunctionSigmoidFahlmanOffset(),false);
            //SpiralVisualisation spiral = new SpiralVisualisation(network);
            //spiral.setVisible(true);
        } catch (Exception ex) {

        }        
        
        /*
        try{
        test.testSpiral("spirals_dif.txt");
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
         * */
        //double d = 1.2345;
        //DecimalFormat format  = new DecimalFormat("#,##0.00");
        //double a = Double.valueOf((format.format(d)));
        //double a = Math.round(d);
        //System.out.println(format.format(d));

    }

}
