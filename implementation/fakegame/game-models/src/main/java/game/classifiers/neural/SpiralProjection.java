/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package game.classifiers.neural;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

/**
 * @author Administrator
 */
public class SpiralProjection extends Canvas {
    private final static int AXIS_UNIT_LENGTH = 40;
    private final static int AXIS_SPLITTER_SIZE = 6;
    private final static int AXIS_DESC_DIST = 20;
    private final static int X_AXIS_DOWN_BOUND = -2;
    private final static int X_AXIS_UP_BOUND = 5;
    private final static int Y_AXIS_DOWN_BOUND = -2;
    private final static int Y_AXIS_UP_BOUND = 5;
    private int originX;
    private int originY;


    public SpiralProjection() {
        this.setOrigin(400, 400);
    }

    public void paintPoint(int x, int y, Color color, Graphics g) {
        g.setColor(color);
        g.drawRect(x, y, 1, 1);
    }

    public void paintAxis(int x, int y, int xDownBound, int xUpBound, int yDownBound, int yUpBound, Color color, Graphics g) {
        this.paintXAxis(x, y, xDownBound, xUpBound, color, g);
        this.paintYAxis(x, y, yDownBound, yUpBound, color, g);


    }

    private void paintXAxis(int x, int y, int xDownBound, int xUpBound, Color color, Graphics g) {
        g.setColor(color);
        int range = xUpBound - xDownBound;
        g.drawString("" + xDownBound, x - 4, y + this.AXIS_DESC_DIST);
        for (int i = 0; i < range; i++) {
            int startX = x + i * this.AXIS_UNIT_LENGTH;
            int endX = startX + this.AXIS_UNIT_LENGTH;
            g.drawLine(startX, y, endX, y);
            g.drawLine(endX, y - this.AXIS_SPLITTER_SIZE / 2, endX, y + this.AXIS_SPLITTER_SIZE / 2);
            String string = "" + (xDownBound + i + 1);
            g.drawString(string, endX - 4, y + this.AXIS_DESC_DIST);
        }
    }

    private void paintYAxis(int x, int y, int yDownBound, int yUpBound, Color color, Graphics g) {
        g.setColor(color);
        int range = yUpBound - yDownBound;
        g.drawString("" + yDownBound, x - this.AXIS_DESC_DIST, y);
        for (int i = 0; i < range; i++) {
            int startY = y - i * this.AXIS_UNIT_LENGTH;
            int endY = startY - this.AXIS_UNIT_LENGTH;
            g.drawLine(x, startY, x, endY);
            g.drawLine(x - this.AXIS_SPLITTER_SIZE / 2, endY, x + this.AXIS_SPLITTER_SIZE / 2, endY);
            String string = "" + (yDownBound + i + 1);
            g.drawString(string, x - this.AXIS_DESC_DIST, endY + 4);
        }
    }

    private void paintOrigin(String string, int x, int y, Color color, Graphics g) {
        g.setColor(color);
        g.drawString(string, x, y);
    }

    private void setOrigin(int x, int y) {
        this.originX = x;
        this.originY = y;
    }

    public void paint(Graphics g) {
        this.paintAxis(this.originX, this.originY, this.X_AXIS_DOWN_BOUND, this.X_AXIS_UP_BOUND, this.Y_AXIS_DOWN_BOUND, this.Y_AXIS_UP_BOUND, Color.BLACK, g);
        this.paintPoint(150, 150, Color.RED, g);
        this.paintPoint(150, 151, Color.BLUE, g);
    }

    public double getXPixelRange() {
        return (this.X_AXIS_UP_BOUND - this.X_AXIS_UP_BOUND) * this.AXIS_UNIT_LENGTH;
    }

    public double getYPixelRange() {
        return (this.Y_AXIS_UP_BOUND - this.Y_AXIS_DOWN_BOUND) * this.AXIS_UNIT_LENGTH;
    }


}
