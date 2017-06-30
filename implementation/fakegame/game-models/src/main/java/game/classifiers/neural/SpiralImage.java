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
public class SpiralImage extends Canvas {

    private final static int CROSS_SIZE = 8;
    private final static int CIRCLE_DIAM = 8;

    private int[][] spiralDatas;
    private int centerX;
    private int centerY;
    private static final int DIAMETER = 13;
    int i = 0;

    public SpiralImage(int numberSpiralElements) {
        spiralDatas = new int[numberSpiralElements][];
        for (int i = 0; i < spiralDatas.length; i++) {
            spiralDatas[i] = new int[3];
        }
        this.setCenter(this.getHeight() / 2, this.getWidth() / 2);
    }

    public SpiralImage(int[][] spiralData) {
        this.spiralDatas = spiralData;
        //this.setCenter(this.getHeight() / 2, this.getWidth() / 2);
        this.setCenter(200, 200);
    }

    public SpiralImage() {
        this.setCenter(this.getHeight() / 2, this.getWidth() / 2);

    }


    public void setSpiralDatas(int[][] spiralDatas) {


    }

    public void paintCross(int x, int y, Color color, Graphics g) {
        g.setColor(color);
        g.drawLine(x + this.centerX, y - this.CROSS_SIZE / 2 + this.centerY, x + this.centerX, y + this.CROSS_SIZE / 2 + this.centerY);
        g.drawLine(x - this.CROSS_SIZE / 2 + this.centerX, y + this.centerY, x + this.CROSS_SIZE / 2 + this.centerX, y + this.centerY);
    }

    public void paintCircle(int x, int y, Color color, Graphics g) {
        g.setColor(color);
        g.drawOval(x - this.CIRCLE_DIAM / 2 + this.centerX, y - this.CIRCLE_DIAM / 2 + this.centerY, this.CIRCLE_DIAM, this.CIRCLE_DIAM);
    }


    public void paintElement(int[] elementData, Graphics g) {
        if (elementData[2] == 0) this.paintCircle(elementData[0], elementData[1], Color.BLUE, g);
        if (elementData[2] == 1) this.paintCross(elementData[0], elementData[1], Color.RED, g);
        //if (elementData[2] == -1) this.paintCircle(elementData[0], elementData[1], Color.YELLOW, g);

    }

    public void paint(Graphics g) {
        for (int i = 0; i < spiralDatas.length; i++) {
            this.paintElement(spiralDatas[i], g);
        }

        /*
        if (i % 2 == 0)g.setColor(Color.red);
        else g.setColor(Color.black);
        g.drawOval(0, 0, 50, 50);
        g.fillOval(200, 200, 50, 50);
        try{
        Thread t = Thread.currentThread();
        t.sleep(2000);
        i++;
        this.repaint();
        }catch(Exception ex){}
         * */
    }

    public void setType(int index, int type) {
        this.spiralDatas[index][2] = type;
    }


    private void setCenter(int x, int y) {
        this.centerX = x;
        this.centerY = y;
    }


}
