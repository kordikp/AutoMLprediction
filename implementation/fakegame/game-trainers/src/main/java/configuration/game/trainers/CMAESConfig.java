package configuration.game.trainers;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import configuration.AbstractCfgBean;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Jul 22, 2009
 * Time: 12:23:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CMAESConfig extends AbstractCfgBean {
    private int rec = 10; //record
    private int draw = 100; //redraw

    private double initialStandardDeviation = 0.2;
    private double stopFitness = 1e-9;
    private int stopMaxFunEvals = 1000;


    public int getRec() {
        return rec;
    }

    public int getDraw() {
        return draw;
    }

    public double getInitialStandardDeviation() {
        return initialStandardDeviation;
    }

    public double getStopFitness() {
        return stopFitness;
    }

    public int getStopMaxFunEvals() {
        return stopMaxFunEvals;
    }
}
