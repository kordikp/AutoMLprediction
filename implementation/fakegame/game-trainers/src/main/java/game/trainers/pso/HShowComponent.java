package game.trainers.pso;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

class HShowComponent extends Canvas implements Runnable, MouseWheelListener {
    private int width;
    private int height;

    int X;

    int Y;

    private double zoom = 1.0;

    double dimensionSize;

    static boolean emphasizeBest;

    HParticle[] data;

    private Thread showThread;

    public Dimension getPreferredSize() {
        return (new Dimension(width, width));
    }

    public HShowComponent(HParticle[] adata, int aX, int aY, int awidth, double avelikost_dimenzi) {
        this.addMouseWheelListener(this);
        height = width = awidth;
        X = aX;
        Y = aY;
        dimensionSize = avelikost_dimenzi;
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
        int x, y;
        for (HParticle aData : data) {
            if (aData != null) {
                x = (int) (width / 2.0 + aData.position[X] / dimensionSize * width * zoom);
                y = (int) (width / 2.0 + aData.position[Y] / dimensionSize * height * zoom);
                //display(g, x, y);
                //displayColor(g, x, y, game.data[index].getAge(), HParticle.getgAge());
                displaySize(g, x, y, aData.getAge());
            }
        }
        if (HShowComponent.emphasizeBest) {
            g.setColor(Color.green);
            x = (int) (width / 2.0 + data[0].position[X] / dimensionSize * width * zoom);
            y = (int) (width / 2.0 + data[0].position[Y] / dimensionSize * height * zoom);
            g.drawLine(x, y - 20, x, y + 20);
            g.drawLine(x - 20, y, x + 20, y);
        }
    }

    public void display(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillOval(x, y, 2, 2);
    }

    public void displayColor(Graphics g, int x, int y, int age, int gAge) {
        setColorByAge(g, age, gAge);
        g.fillOval(x, y, 2, 2);
    }

    void displaySize(Graphics g, int x, int y, int age) {
        age = Math.min(age, 30);
        g.drawOval(x - age / 2, y - age / 2, age, age);
    }

    void setColorByAge(Graphics gr, int age, int gAge) {
        double ratio = (double) age / gAge;
        int r, g, b;
        r = 255;
        g = b = (int) (200 * (1 - ratio));
        gr.setColor(new Color(r, g, b));
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
