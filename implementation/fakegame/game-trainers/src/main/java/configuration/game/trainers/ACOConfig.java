/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Ant Colony Optimization (ACO*)- configuration</p>
 * <p>Description: class for ACO* configuration</p>
 */

package configuration.game.trainers;


import configuration.AbstractCfgBean;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.ui.CheckBox;

public class ACOConfig extends AbstractCfgBean {
    private static final long serialVersionUID = 1L;

    @Property(name = "Record error", description = "Error recorded every ... fitness function call")
    private int rec;
    @Property(name = "Redraw screen", description = "Screen redrawed every ... fitness function call")
    private int draw;

    @Property(name = "Max Iterations", description = "Maximum number of iterations (training stops, when reached)!")
    private int maxIterations;          // maximum iterations of algorithm
    @Property(name = "Max Stagnation", description = "Maximum iterations without improvement (training stops, when reached)!")
    private int maxStagnation;          // maximum iterations without improvement
    @Property(name = "Population Size", description = "Number of ants in the population")
    private int populationSize;         // number of ants in population
    // double  minAcceptableError = 0;    // minimal acceptable error
    @Property(name = "Deviation parameter", description = "Lower value - better solutions are strongly preferred")
    private double q;                  // in omega: deviation parameter. lower - better solutions are strongly preferred


    @Property(name = "Convergence parameter", description = "Lower value -faster convergence")
    private double r;                  // in sigma: speed of convergence parameter. lower - faster convergence
    @Property(name = "Replace ants", description = "Number of ants to replace in the population")
    private int replace;            // number of ants to replace in one iteration
    @Property(name = "Standard deviation used", description = "When allowed, std dev is used, otherwise an average is used")
    @CheckBox
    private boolean standardDeviation;  // use standard deviation? otherwise average is used
    @Property(name = "Force diversity", description = "Limit the neigbourhood for the deviation computation by diversityLimit")
    @CheckBox
    private boolean forceDiversity;     // limit neigbourhood for the deviation computation by diversityLimit
    @Property(name = "Diversity limit", description = "Size of neigborhood for forceDiversity")

    private double diversityLimit;     // size of neigborhood for forceDiversity
    @Property(name = "Gradient Weight", description = "The gradient of the error surface contribution (0-no, 1=highest)")
    private double gradientWeight;     // gradient impact
    // boolean graphicsOn;         // show window with graphical info?
    @Property(name = "Debug Enabled", description = "Debug mode on/off")
    @CheckBox
    private boolean debugOn;            // print debug info


    /**
     * inicialises parametres to its default values
     */
    public ACOConfig() {
        populationSize = 60;
        maxIterations = 200;
        maxStagnation = 30;
        // minAcceptableError  = 0;
        q = 0.8;
        r = 0.4;
        replace = 60;
        standardDeviation = true;
        forceDiversity = false;
        diversityLimit = 0.1;
        gradientWeight = 0.0;
        // graphicsOn  = false;
        debugOn = false;
    }

    public int getRec() {
        return rec;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setMaxStagnation(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public void setR(double r) {
        this.r = r;
    }

    public void setReplace(int replace) {
        this.replace = replace;
    }

    public void setDiversityLimit(double diversityLimit) {
        this.diversityLimit = diversityLimit;
    }

    public void setGradientWeight(double gradientWeight) {
        this.gradientWeight = gradientWeight;
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

    public boolean isStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(boolean standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public boolean isForceDiversity() {
        return forceDiversity;
    }

    public void setForceDiversity(boolean forceDiversity) {
        this.forceDiversity = forceDiversity;
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

    public int getReplace() {
        return replace;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxStagnation() {
        return maxStagnation;
    }

    // public double  getMinAcceptableError() { return minAcceptableError; }
    public double getR() {
        return r;
    }

    public double getQ() {
        return q;
    }

    public boolean getStandardDeviation() {
        return standardDeviation;
    }

    public boolean getForceDiversity() {
        return forceDiversity;
    }

    public double getDiversityLimit() {
        return diversityLimit;
    }

    public double getGradientWeight() {
        return gradientWeight;
    }

    // public boolean getGraphicsOn() { return graphicsOn; }
    public boolean getDebugOn() {
        return debugOn;
    }


}
