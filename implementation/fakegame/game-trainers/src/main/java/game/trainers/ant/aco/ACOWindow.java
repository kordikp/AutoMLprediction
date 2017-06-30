/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.0
 * <p>
 * <p>Title: Ant Colony Optimization (ACO*) Visualization</p>
 * <p>Description: classes for ACO* visualisation</p>
 */

package game.trainers.ant.aco;

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

public class ACOWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private ACOPanel p;
    JPanel pButtons;
    JButton bRangeXP;
    JButton bRangeXM;

    public ACOWindow() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
/*    	pButtons = new JPanel();
        bRangeXP = new JButton("+");
    	pButtons.add(bRangeXP);
    	bRangeXM = new JButton("-");
    	pButtons.add(bRangeXM);
 */
        p = new ACOPanel();
        setTitle("Ant Colony Optimization");
        //setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
/*        this.setLayout(new java.awt.FlowLayout());
        getContentPane().add(pButtons);
       p.setPreferredSize(new Dimension(800, 500));*/
        getContentPane().add(p);
        setSize(new Dimension(1000, 500));
        setLocationRelativeTo(null);
    }

/*    public void actionPerformed(ActionEvent e) {
        if ((e.getSource()== bRangeXP)) {
	    	p.range *= 1.5;
	    } else if ((e.getSource()== bRangeXM)) {
	    	p.range /= 1.5;
	    }
    }*/

    public void setColony(Colony colony) {
        ACOPanel.colony = colony;
    }

    public void paintComponent(Graphics g) {
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

class ACOPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static Colony colony;
    private BufferedImage bi;
    private Graphics2D big;
    private Rectangle area;
    private boolean firstTime = true;
    private double error;

    private double range = 10.0;

    public ACOPanel() {
        setBackground(Color.white);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        update(g);
    }

    public void update(Graphics g) {
        BasicStroke thinStroke = new BasicStroke(1);
        BasicStroke thickStroke = new BasicStroke(3);
        Color darkGreen = new Color(0, 160, 0);

        Graphics2D g2 = (Graphics2D) g;
        Dimension dim = getSize();

        int w = dim.width;
        int h = dim.height;
        int margintop = 10;
        int marginbottom = 10;
        int marginleft = 140;
        int marginright = 80;
        int graphw = w - marginleft - marginright;
        int graphh = h - margintop - marginbottom;
        int axish = 20;
        int rowheight = (graphh / colony.dimensions);

        double coefx = graphw / (2 * range);
        double coefy = 2000.0;

        double step = range / 10.0;

        int minx = marginleft + (graphw / 2) + (int) Math.round(-range * coefx);
        int maxx = marginleft + (graphw / 2) + (int) Math.round(range * coefx);

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

        big.drawString("Error: " + (double) Math.round(error * 1000) / 1000.0, 20, 20);
        double mean;
        double weight;
        double deviation;

        double y;
        int axisy;
        int antx;

        for (int dimension = 0; dimension < colony.dimensions; dimension++) {
            big.setStroke(thinStroke);
            // paint axis
            axisy = h - marginbottom - (rowheight * dimension) - axish;
            big.setPaint(Color.black);
            big.drawLine(marginleft, axisy, marginleft + graphw, axisy);

            int axisx;
            for (double x = -range; x <= range; x += step) {
                axisx = marginleft + (graphw / 2) + (int) Math.round(x * coefx);
                big.drawLine(axisx, axisy, axisx, axisy + 2);
                big.drawString(Double.toString(x), axisx - 8, axisy + 13);
            }

            // for all ants
            for (int i = 0; i < colony.populationSize; i++) {
                weight = colony.ants[i].gWeight;
                mean = colony.ants[i].pVector[dimension];


                deviation = 0.0;
                int nearestCount = 0;

                for (int n = 0; n < colony.populationSize; n++) {
                    double tmp = (colony.ants[n].pVector[dimension] - mean);
                    if (!colony.standardDeviation) {
                        if ((!colony.forceDiversity) || (tmp < colony.diversityLimit)) {
                            deviation += Math.abs(tmp);
                            nearestCount++;
                        }
                    } else {
                        if ((!colony.forceDiversity) || (tmp < colony.diversityLimit)) {
                            deviation += tmp * tmp;
                            nearestCount++;
                        }
                    }
                }
                if (!colony.standardDeviation)
                    deviation /= nearestCount;
                else {
                    deviation /= (nearestCount - 1);
                    deviation = Math.sqrt(deviation);
                }
                deviation *= colony.r;

                // paint gaussian curve
                big.setPaint(Color.black);
                int oldx = marginleft + (graphw / 2) + (int) Math.round(-range * coefx);
                int oldy = axisy;
                int newx, newy;
                for (double x = -range; x <= range; x += step / 10) {
                    y = coefy * weight * (1 / (deviation * Math.sqrt(2 * Math.PI))) * Math.exp(-(Math.pow(x - mean, 2.0)) / (2 * Math.pow(deviation, 2.0)));
                    newy = axisy - (int) Math.round(y);
                    newx = marginleft + (graphw / 2) + (int) Math.round(x * coefx);
                    big.drawLine(oldx, oldy, newx, newy);
                    oldx = newx;
                    oldy = newy;
                }

                big.setPaint(Color.red);
                antx = marginleft + (graphw / 2) + (int) Math.round(mean * coefx);
                if ((antx > minx) && (antx < maxx))
                    big.drawLine(antx, axisy, antx, axisy - (rowheight - axish));
            }

            big.setPaint(darkGreen);
            big.drawString(Double.toString((double) Math.round(colony.gBestVector[dimension] * 1000) / 1000.0), marginleft + graphw + 20, axisy);

            big.setStroke(thickStroke);
            antx = marginleft + (graphw / 2) + (int) Math.round(colony.gBestVector[dimension] * coefx);
            if ((antx > minx) && (antx < maxx))
                big.drawLine(antx, axisy, antx, axisy - (rowheight - axish));
        }

        // Draws the buffered image to the screen.
        g2.drawImage(bi, 0, 0, this);

    }

    public void setError(double error) {
        this.error = error;
    }
}
