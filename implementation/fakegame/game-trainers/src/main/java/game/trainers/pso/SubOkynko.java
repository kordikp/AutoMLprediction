package game.trainers.pso;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

class SubOkynko extends JFrame {
    public SubOkynko(int dimenze_1, int dimenze_2, double velikost_dimenzi, Ptak[] data) {
        try {
            jbInit(dimenze_1, dimenze_2, velikost_dimenzi, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit(int dimenze_1, int dimenze_2, double velikost_dimenzi, Ptak[] data) throws Exception {
        setTitle("PSO: " + dimenze_1 + " & " + dimenze_2);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(new ShowComponent(data, dimenze_1, dimenze_2, 300, velikost_dimenzi));
        pack();
//        setLocationRelativeTo(null);
    }
}
