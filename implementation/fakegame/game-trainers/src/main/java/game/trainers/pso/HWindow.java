package game.trainers.pso;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

class HWindow extends JFrame implements MouseListener {

    public HWindow(int dims, double dimSize, HParticle[] data) {
        try {
            jbInit(dims, dimSize, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit(int dims, double dimSize, HParticle[] data) throws Exception {
        int gX, gY;
        setTitle("Hybrid of Genetic algorithm and Particle swarm optimization");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gY = dims - 1;
        gX = 0;
        for (int i = 1; i < dims; i++)
            gX += i;
        gX = (gX / gY);
        getContentPane().setLayout(new GridLayout(gX, gY));
        for (int x = 1; x < dims; x++)
            for (int y = x + 1; y <= dims; y++) {
                HShowComponent s = new HShowComponent(data, x - 1, y - 1, 150, dimSize);
                getContentPane().add(s).addMouseListener(this);
            }
        pack();
        setLocationRelativeTo(null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        HShowComponent s = (HShowComponent) e.getSource();
        HSubWindow so = new HSubWindow(s.X, s.Y, s.dimensionSize, s.data);
        so.setVisible(true);
    }

    public void mouseReleased(MouseEvent e) {
    }
}
