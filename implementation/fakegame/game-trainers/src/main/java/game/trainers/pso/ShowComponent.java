package game.trainers.pso;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

class ShowComponent extends Canvas implements Runnable, MouseWheelListener {
    private int width;
    private int height;
    int X;
    int Y;
    private double zoom = 1.0;
    double velikost_dimenzi;
    Ptak[] data;
    private Thread showThread;

    public Dimension getPreferredSize() {
        return (new Dimension(width, width));
    }

    public ShowComponent(Ptak[] adata, int aX, int aY, int awidth, double avelikost_dimenzi) {
        this.addMouseWheelListener(this);
        height = width = awidth;
        X = aX;
        Y = aY;
        velikost_dimenzi = avelikost_dimenzi;
        data = adata;
        setBackground(Color.WHITE);
        start();
    }

    public void paint(Graphics g) {
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        g.setColor(Color.blue);
        g.drawLine(0, height / 2, width, height / 2);
        g.drawLine(width / 2, 0, width / 2, height);
        g.drawString("" + X + "; " + Y + ";Z=" + zoom, 3, 13);
        g.setColor(Color.red);
        double x, y;
        for (Ptak aData : data) {
            x = width / 2.0 + aData.present[X] / velikost_dimenzi * width * zoom;
            y = width / 2.0 + aData.present[Y] / velikost_dimenzi * height * zoom;
            g.fillOval((int) (x), (int) (y), 2, 2);
        }
    }

    public void run() {
        while (showThread != null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            repaint();
        }
    }

    void start() {
        showThread = new Thread(this);
        showThread.setPriority(Thread.MIN_PRIORITY);
        showThread.start();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == 1) zoom *= 2.0d;
        else zoom /= 2.0d;
    }
}
