package configuration.game.trainers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import configuration.AbstractCfgBean;

/**
 * @author weberj1
 */
public class PSOConfig extends AbstractCfgBean {
    int maxIterations;             // maximal number of method's outer loop iterations
    int maxStagnation;             // maximum stagnation
    int populationSize;            // number of ants in population
    double initMin;                // minimum for random initialization
    double initMax;                // maximum for random initialization
    double phi1;                   // cognitive acceleration coefficient
    double phi2;                   // social acceleration coefficient
    boolean debugOn;               // debug flag


    // text fields for parameters
    transient private JTextField textMaxIterations;
    transient private JTextField textMaxStagnation;
    transient private JCheckBox chckDebugOn;
    transient private JTextField textPopulationSize;
    transient private JTextField textInitMin;
    transient private JTextField textInitMax;
    transient private JTextField textPhi1;
    transient private JTextField textPhi2;

    transient private JButton bSave;
    transient private JPanel p1;

    public PSOConfig() {


        maxIterations = 800; // maximal number of method's outer loop iterations
        maxStagnation = 100;             // maximum stagnation
        populationSize = 10;            // number of ants in population
        initMin = -10.0;             // minimum for random initialization
        initMax = 10.0;              // maximum for random initialization
        phi1 = 2.0;                  // cognitive acceleration coefficient
        phi2 = 2.0;                  // social acceleration coefficient
        debugOn = false;
    }


    void makelabel(String name, GridBagLayout gridbag, GridBagConstraints c) {
        JLabel label = new JLabel(name);
        gridbag.setConstraints(label, c);
        p1.add(label);
    }


    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    public void setMaxStagnation(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getInitMin() {
        return initMin;
    }

    public void setInitMin(double initMin) {
        this.initMin = initMin;
    }

    public double getInitMax() {
        return initMax;
    }

    public void setInitMax(double initMax) {
        this.initMax = initMax;
    }

    public double getPhi1() {
        return phi1;
    }

    public void setPhi1(double phi1) {
        this.phi1 = phi1;
    }

    public double getPhi2() {
        return phi2;
    }

    public void setPhi2(double phi2) {
        this.phi2 = phi2;
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }
}


