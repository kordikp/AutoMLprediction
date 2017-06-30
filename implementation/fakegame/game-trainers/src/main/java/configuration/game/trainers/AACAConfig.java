/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: Adaptive Ant Colony Algorithm (AACA) - configuration</p>
 * <p>Description: class for AACA configuration</p>
 */

package configuration.game.trainers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import configuration.AbstractCfgBean;
import org.ytoh.configurations.annotations.Component;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.CheckBox;


@Component(name = "AACATrainerConfig", description = "AACA algorithm configuration")

public class AACAConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;

    @Property(name = "Record error", description = "Error recorded every ... fitness function call")
    private int rec;
    @Property(name = "Redraw screen", description = "Screen redrawed every ... fitness function call")
    private int draw;

    @Property(name = "Max Iterations", description = "Maximum number of iterations (training stops, when reached)!")
    private int maxIterations;          // maximum iterations of algorithm
    @Property(name = "Max Stagnation", description = "Maximum iterations without improvement (training stops, when reached)!")
    private int maxStagnation;          // maximum iterations without improvement
    // double minAcceptableError;  // minimal acceptable error
    @Property(name = "Debug Enabled", description = "Debug mode on/off")
    @CheckBox
    private boolean debugOn;            // print debug info
    // boolean graphicsOn;         // show visualization window
    @Property(name = "Population Size", description = "Number of ants in the population")
    private int populationSize;         // number of ants in population
    @Property(name = "Encoding Length", description = "Number of bits in searching variables")
    private int encodingLength;         // number of bits in searching variables
    @Property(name = "Evaporation Factor", description = "Pheromone evaporates faster -> 1")
    private double evaporationFactor;   // lambda <0,1>
    @Property(name = "Pheromone Index", description = "Beta - ant like behaviour")
    private double pheromoneIndex;      // beta >= 0
    @Property(name = "Cost Index", description = "Delta - heuristic behaviour")
    private double costIndex;           // delta >= 0
    @Property(name = "Parameter Minimum - limits the search space")
    private double min;                 // parameter minimum
    @Property(name = "Parameter Maximum - limits the search space")
    private double max;                 // parameter maximum
    @Property(name = "Gradient Weight", description = "The gradient of the error surface contribution (0-no, 1=highest)")
    private double gradientWeight;     // gradient impact
    // transient AACAWindow window;


    /**
     * inicialises parametres to its default values
     */
    public AACAConfig() {
        maxIterations = 2000;
        maxStagnation = 200;
        // minAcceptableError = 0;
        debugOn = false;
        // graphicsOn = false;

        populationSize = 20;
        encodingLength = 8;
        evaporationFactor = 0.8;
        pheromoneIndex = 0.1;
        costIndex = 0.0;
        min = -10.0;
        max = 10.0;
        gradientWeight = 0.0;

        // window = new AACAWindow();
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    // public double getMinAcceptableError() { return minAcceptableError; }
    public boolean getDebugOn() {
        return debugOn;
    }
    // public boolean getGraphicsOn() { return graphicsOn; }

    public int getPopulationsize() {
        return populationSize;
    }

    public int getEncodingLength() {
        return encodingLength;
    }

    public double getEvaporationFactor() {
        return evaporationFactor;
    }

    public double getPheromoneIndex() {
        return pheromoneIndex;
    }

    public double getCostIndex() {
        return costIndex;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getGradientWeight() {
        return gradientWeight;
    }

    public int getRec() {
        return rec;
    }

    public void setRec(int rec) {
        this.rec = rec;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setMaxStagnation(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    public void setEncodingLength(int encodingLength) {
        this.encodingLength = encodingLength;
    }

    public void setEvaporationFactor(double evaporationFactor) {
        this.evaporationFactor = evaporationFactor;
    }

    public void setPheromoneIndex(double pheromoneIndex) {
        this.pheromoneIndex = pheromoneIndex;
    }

    public void setCostIndex(double costIndex) {
        this.costIndex = costIndex;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setGradientWeight(double gradientWeight) {
        this.gradientWeight = gradientWeight;
    }

//  public AACAWindow getWindow() { return window; }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path        file path
     * @param description file description
     * @return image icon\
     */
    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = AACAConfig.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("File not found: " + path);
            return null;
        }
    }
}
