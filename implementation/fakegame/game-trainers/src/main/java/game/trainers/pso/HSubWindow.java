package game.trainers.pso;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

class HSubWindow extends JFrame {
    public HSubWindow(int dimenze_1, int dimenze_2, double velikost_dimenzi, HParticle[] data) {
        try {
            jbInit(dimenze_1, dimenze_2, velikost_dimenzi, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit(int dimenze_1, int dimenze_2, double velikost_dimenzi, HParticle[] data) throws Exception {
        setTitle("HGAPSO: " + dimenze_1 + " & " + dimenze_2);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(new HShowComponent(data, dimenze_1, dimenze_2, 300, velikost_dimenzi));
        pack();
//        setLocationRelativeTo(null);
    }
}
