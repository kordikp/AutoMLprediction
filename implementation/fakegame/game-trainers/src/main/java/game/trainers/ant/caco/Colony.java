/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Continuous Ant Colony Optimization (CACO) - colony</p>
 * <p>Description: class for CACO colony</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 * <p>
 * <p>Source: Ant Colony Optimization for Continuous Spaces
 * Kuhn L.
 * A thesis submitted to The Department of Information Technology and Electrical
 * Engineering The University of Queensland, October 2002.
 * http://innovexpo.itee.uq.edu.au/2002/projects/s354170/thesis.pdf
 * </p>
 */

package game.trainers.ant.caco;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Random;

public class Colony {

    // window for visualisation
    // CACOWindow window;

    // parameters of algorithm
    int directionsCount;     // number of directions to search
    // double minAcceptableError;  // minimal acceptable error
    private int maxIterations;       // maximum of iterations
    private int maxStagnation;       // iterations without improvement
    double searchRadius;        // initial search radius
    private double radiusMultiplier;    // search radius decrease speed
    private int radiusGeneration;    // generations befora decrease
    private double startingPheromone;   // initial pheromone amount
    private double minimumPheromone;    // minimum pheromone amount
    private double addPheromone;        // pheromone amount to add
    private double evaporation;
    private double gradientWeight;     // gradient heuristic impact


    private boolean debugOn;
    // boolean graphicsOn;         // visualisation window

    private DecimalFormat df;
    private int iteration;              // current iteration
    private int stagnation;             // iteration withou improvement
    private Uncmin_methods trainer;
    int dimensions;             // number of variables to optimize

    private double[] nest;              //nest coordinates
    Direction directions[];     // directions (positions, pheromone)

    private Random generator;

    private double[] gBestVector; // globaly best variables vector
    private double gBestError;    // its error


    /**
     * constructor - saves parameters
     *
     * @param train
     * @param dimensions
     * @param maxIterations
     * @param maxStagnation
     * @param searchRadius
     * @param directionsCount
     * @param radiusMultiplier
     * @param radiusGeneration
     * @param startingPheromone
     * @param minimumPheromone
     * @param addPheromone
     * @param evaporation
     * @param gradientWeight
     * @param debugOn
     */
    /* public Colony(Uncmin_methods train, int dimensions, int maxIterations,
            int maxStagnation, double searchRadius, int directionsCount,
            double radiusMultiplier, int radiusGeneration, double startingPheromone,
            double minimumPheromone, double addPheromone, double evaporation,
            boolean debugOn, boolean graphicsOn, CACOWindow window) { */
    public Colony(Uncmin_methods train, int dimensions, int maxIterations,
                  int maxStagnation, double searchRadius, int directionsCount,
                  double radiusMultiplier, int radiusGeneration, double startingPheromone,
                  double minimumPheromone, double addPheromone, double evaporation,
                  double gradientWeight, boolean debugOn) {

        // connect visualization window with colony
        // this.window = window;
        // if (window!=null) window.setColony(this);

        generator = new Random();

        this.searchRadius = searchRadius;
        this.directionsCount = directionsCount;
        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        // this.minAcceptableError = minAcceptableError;

        this.radiusMultiplier = radiusMultiplier;
        this.radiusGeneration = radiusGeneration;
        this.startingPheromone = startingPheromone;
        this.minimumPheromone = minimumPheromone;
        this.addPheromone = addPheromone;
        this.evaporation = evaporation;

        this.dimensions = dimensions;
        this.gradientWeight = gradientWeight;

        this.debugOn = debugOn;
        // this.graphicsOn = graphicsOn;
        // this.window = window;
        trainer = train;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(4);

        // Direction.firstInit(trainer, dimensions);

        // random nest position
        nest = new double[directionsCount];

        // random directions
        directions = new Direction[directionsCount];
/*        for (int i = 0; i < directionsCount; i++)
            directions[i] = new Direction(startingPheromone, minimumPheromone,
                    addPheromone, evaporation, dimensions, trainer, gradientWeight);*/
        gBestError = Double.POSITIVE_INFINITY;
    }

    /**
     * main purpose for this is destoying window
     */
    public void destroy() {
        df = null;
        nest = null;
        directions = null;
        generator = null;
    }

    /**
     * directions
     */
    void colonyDump() {
        for (int i = 0; i < directionsCount; i++) {
            System.out.print(df.format(directions[i].getPheromone()) + " ");
        }
        System.out.println();
    }

    /**
     * main body of algorithm
     */
    public void run() {
        // double error = 0.0;

        // place nest
        for (int i = 0; i < directionsCount; i++)
            nest[i] = (Math.random() * 20.0) - 10.0;

        // randomize search directions
        for (int i = 0; i < directionsCount; i++)
            directions[i] = new Direction(startingPheromone, minimumPheromone,
                    addPheromone, evaporation, dimensions, trainer, gradientWeight);

        iteration = 1;
        stagnation = 0;
        // for all ants
        //     choose direction, make random walk
        //     on improvement add pheromone
        //     decrease search diameter every n steps
        do {
            int vecnum;
            double rand;
            double sum = 0;

            for (int d = 0; d < directionsCount; d++)
                sum += directions[d].getPheromone();

            rand = generator.nextDouble() * sum;
            vecnum = 0;
            sum = 0;
            while (sum < rand) {
                sum += directions[vecnum].getPheromone();
                vecnum++;
            }
            if (vecnum > 0) vecnum--;

            directions[vecnum].explore(searchRadius);


            if (iteration % radiusGeneration == 0) {
                searchRadius = searchRadius * radiusMultiplier;
                for (int i = 0; i < directionsCount; i++)
                    directions[i].evaporatePheromone();
                if (debugOn)
                    System.out.println("Iteration: " + iteration + "; Radius: " + df.format(searchRadius) + "; error: " + df.format(gBestError));
                if (debugOn) colonyDump();
            }

            // if (graphicsOn && (window!=null)) window.dopaint();

            // find direciton with best error
            for (int d = 0; d < directionsCount; d++) {
                if (directions[d].getgBestError() < gBestError) {
                    gBestError = directions[d].getgBestError();
                    stagnation = 0;
                }
            }

            // distribure best error
            for (int d = 0; d < directionsCount; d++) {
                directions[d].setgBestError(gBestError);
            }

            //error = Direction.gBestError;
        } while ((stagnation++ < maxStagnation) && (iteration++ < maxIterations));

    }

    /**
     * returns global best solution
     *
     * @param i
     * @return
     */
    public double getBest(int i) {
        return gBestVector[i];
    }

    /**
     * returns global best error
     *
     * @return
     */
    public double getError() {
        return gBestError;
    }
}

