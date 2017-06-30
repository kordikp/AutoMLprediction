/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Adaptive Ant Colony Optimization (AACA)</p>
 * <p>Description: classes for AACA visualization</p>
 */

package game.trainers.ant.aaca;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

class AACAWindow extends JFrame {
    // private static final long serialVersionUID = 1L;

    private AACAPanel p;
    JPanel pButtons;
    JButton bRangeXP;
    JButton bRangeXM;

    public AACAWindow() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        p = new AACAPanel();
        setTitle("Adaptive Ant Colony Algorithm (AACA) visualization");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        getContentPane().add(p);
        setSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
    }

    public void setColony(Colony colony) {
        p.colony = colony;
    }

    void paintComponent(Graphics g) {
        //this.paintComponent(g);
        p.repaint();
    }

    public void dopaint() {
        p.update(p.getGraphics());
        update(this.getGraphics());
    }

    public void update(Graphics g) {
        super.update(g);
    }
}

class AACAPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public Colony colony;
    private BufferedImage bi;
    private Graphics2D big;
    private Rectangle area;
    private boolean firstTime = true;
    private double error;

    double range = 10.0;

    public AACAPanel() {
        setBackground(Color.white);
    }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        update(g);
    }

    public void update(Graphics g) {
        Color darkGreen = new Color(0, 160, 0);
        Color background = new Color(237, 233, 227);

        Graphics2D g2 = (Graphics2D) g;
        Dimension dim = getSize();

        int w = dim.width;
        int h = dim.height;
        int margintop = 10;
        int marginbottom = 10;
        int marginleft = 10;
        int marginright = 80;
        int graphw = w - marginleft - marginright;
        int graphh = h - margintop - marginbottom;
        int rowheight = (graphh / colony.dimensions);

        if (firstTime) {
            bi = (BufferedImage) createImage(w, h);
            big = bi.createGraphics();
            area = new Rectangle(dim);
            firstTime = false;
        }

        // Clears the rectangle that was previously drawn.
        big.setColor(Color.white);
        big.clearRect(0, 0, area.width, area.height);

        // Draws and fills the newly positioned rectangle to the buffer.
        big.setPaint(Color.black);

        // paint circles
        int diameter = 15;
        int spacing = (graphw - 40) / colony.encodingLength;

        // for all dimensions
        for (int d = 0; d < colony.dimensions; d++) {

            // draw lines between nodes
            big.setPaint(Color.BLUE);
            //  S->0
            big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneFirst[d][0] / 5.0)));
            big.drawLine(marginleft + 20, margintop + d * rowheight + rowheight / 2,
                    marginleft + 20 + spacing, margintop + (d) * rowheight + rowheight / 5);
            //  S->1
            big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneFirst[d][1] / 5.0)));
            big.drawLine(marginleft + 20, margintop + d * rowheight + rowheight / 2,
                    marginleft + 20 + spacing, margintop + (d + 1) * rowheight - rowheight / 5);


            for (int e = 0; e < (colony.encodingLength - 1); e++) {
                // 0->0
                big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneOther[d][e][0][0] / 5.0)));
                big.drawLine(marginleft + 20 + spacing * (e + 1), margintop + (d) * rowheight + rowheight / 5,
                        marginleft + 20 + spacing * (e + 2), margintop + (d) * rowheight + rowheight / 5);
                // 1->1
                big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneOther[d][e][1][1] / 5.0)));
                big.drawLine(marginleft + 20 + spacing * (e + 1), margintop + (d + 1) * rowheight - rowheight / 5,
                        marginleft + 20 + spacing * (e + 2), margintop + (d + 1) * rowheight - rowheight / 5);

                // 0->1
                big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneOther[d][e][0][1] / 5.0)));
                big.drawLine(marginleft + 20 + spacing * (e + 1), margintop + (d) * rowheight + rowheight / 5,
                        marginleft + 20 + spacing * (e + 2), margintop + (d + 1) * rowheight - rowheight / 5);
                // 1->0
                big.setStroke(new BasicStroke((float) (colony.pheromone.pheromoneOther[d][e][1][0] / 5.0)));
                big.drawLine(marginleft + 20 + spacing * (e + 1), margintop + (d + 1) * rowheight - rowheight / 5,
                        marginleft + 20 + spacing * (e + 2), margintop + (d) * rowheight + rowheight / 5);
            }

            // draw nodes
            big.setPaint(Color.BLACK);
            for (int e = 1; e <= colony.encodingLength; e++) {
                big.fillOval((marginleft + 20 + spacing * e) - diameter, (margintop + (d) * rowheight + rowheight / 5) - diameter,
                        2 * diameter, 2 * diameter);
                big.fillOval((marginleft + 20 + spacing * e) - diameter, (margintop + (d + 1) * rowheight - rowheight / 5) - diameter,
                        2 * diameter, 2 * diameter);
            }
            big.fillOval((marginleft + 20) - diameter, (margintop + d * rowheight + rowheight / 2) - diameter,
                    2 * diameter, 2 * diameter);

            // inner fill of nodes
            big.setPaint(background);
            for (int e = 1; e <= colony.encodingLength; e++) {
                big.fillOval((marginleft + 20 + spacing * e) - diameter + 2, (margintop + (d) * rowheight + rowheight / 5) - diameter + 2,
                        2 * diameter - 4, 2 * diameter - 4);
                big.fillOval((marginleft + 20 + spacing * e) - diameter + 2, (margintop + (d + 1) * rowheight - rowheight / 5) - diameter + 2,
                        2 * diameter - 4, 2 * diameter - 4);
            }
            big.fillOval((marginleft + 20) - diameter + 2, (margintop + d * rowheight + rowheight / 2) - diameter + 2,
                    2 * diameter - 4, 2 * diameter - 4);

            // node numbers
            big.setPaint(Color.BLACK);
            for (int e = 1; e <= colony.encodingLength; e++) {
                big.drawString("0", marginleft + 20 + spacing * e - 4, margintop + (d) * rowheight + rowheight / 5 + 5);
                big.drawString("1", marginleft + 20 + spacing * e - 4, margintop + (d + 1) * rowheight - rowheight / 5 + 5);
            }

            // real values
            big.setPaint(darkGreen);
            big.drawString("P" + d + " = " + (double) Math.round(colony.values[d] * 1000) / 1000.0, marginleft + graphw, margintop + d * rowheight + rowheight / 2);
        }

        // error
        big.setPaint(Color.RED);
        big.drawString("Error: " + (double) Math.round(colony.gBestError * 1000) / 1000.0, 20, 20);

        // Draws the buffered image to the screen.
        g2.drawImage(bi, 0, 0, this);

    }

    public void setError(double error) {
        this.error = error;
    }
}
