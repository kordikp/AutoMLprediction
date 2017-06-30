/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Ant Colony Optimization (ACO*) - colony</p>
 * <p>Description: class for ACO* colony</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 * <p>
 * <p>Source: Training feed-forward neural networks with ant colony optimization: An application to pattern classification
 * Blum Ch., Socha K.
 * </p>
 */

package game.trainers.ant.aco;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Colony {
    // ACOWindow window;           // window with graphical informations about colony

    // parameters of algorithm
    int populationSize;     // number of ants
    private int maxIterations;      // maximum iterations
    private int maxStagnation;      // maximum iterations without improvement
    // double  minAcceptableError; // minimal acceptable
    private double q;                  // deviation parameter. lower - better solutions are strongly preferred
    double r;                  // from (0, 1): speed of convergence parameter. lower - faster convergence
    private int replace;            // number of ants to replace in one iteration
    boolean deviationBug;       // turns on wrong deviation comupation (gives better results!)
    boolean standardDeviation;  // use standard deviation? otherwise average is used
    boolean forceDiversity;     // limit neigbourhood for the deviation computation by diversityLimit
    double diversityLimit;     // size of neigborhood for forceDiversity
    private double gradientWeight;     // gradient heuristic impact

    // boolean graphicsOn;
    private boolean debugOn;

    private DecimalFormat df;
    private int iteration = 1;      // current iteration
    private int stagnation = 0;     // iterations without improvement
    private Uncmin_methods trainer;     // trainer object
    int dimensions;         // number of variables to optimize

    Ant ants[];                 // ants in colony

    private Random generator;

    public double gBestVector[];    // globaly best variables vector
    private double gBestError;        // its error
    private double[] lBestVector;    // local best position
    private double lBestError;       // local best error

    private double[] newAntPos;

    /**
     * prints information about ant in colony
     */
    public void colonyDump() {
        for (int i = 0; i < populationSize; i++) {
            System.out.print("Ant " + i + ": weight=" + df.format(ants[i].gWeight) + " error=" + df.format(ants[i].pError) + "  values: ");
            for (int j = 0; j < ants[i].dimensions; j++) {
                System.out.print(df.format(ants[i].pVector[j]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * constructor - saves parameters
     *
     * @param trainer           trainer object
     * @param dimensions        number of variables to optimize
     * @param populationSize    number of ants
     * @param maxIterations     maximum iterations
     * @param maxStagnation     maximum iterations without improvement
     * @param q                 deviation parameter. lower - better solutions are strongly preferred
     * @param r                 from (0, 1): speed of convergence parameter. lower - faster convergence
     * @param replace           number of ants to replace in one iteration
     * @param standardDeviation use standard deviation? otherwise average is used
     * @param forceDiversity    limit neigbourhood for the deviation computation by diversityLimit
     * @param diversityLimit    size of neigborhood for forceDiversity
     * @param gradientWeight    gradient heuristic impact
     * @param debugOn           display debug messages
     */
    public Colony(Uncmin_methods trainer, int dimensions, int populationSize,
                  int maxIterations, int maxStagnation, double q, double r,
                  int replace, boolean standardDeviation, boolean forceDiversity,
                  double diversityLimit, double gradientWeight, boolean debugOn) {
        this.populationSize = populationSize;
        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        // this.minAcceptableError = minAcceptableError;
        this.q = q;
        this.r = r;
        this.replace = replace;
        this.dimensions = dimensions;
        this.standardDeviation = standardDeviation;
        this.forceDiversity = forceDiversity;
        this.diversityLimit = diversityLimit;
        this.gradientWeight = gradientWeight;
        // this.graphicsOn = graphicsOn;
        this.debugOn = debugOn;
        this.trainer = trainer;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);

        // if (graphicsOn) window = new ACOWindow();

        ants = new Ant[populationSize + replace];
        for (int i = 0; i < populationSize + replace; i++) ants[i] = new Ant(dimensions);

        gBestVector = new double[dimensions];
        lBestVector = new double[dimensions];

        newAntPos = new double[dimensions];

        generator = new Random();
    }

    /**
     * destoying window (disputable effect)
     */
    public void destroy() {
        df = null;
        // window = null;
        ants = null;
        generator = null;
    }


    /**
     * counts error for ant from its possition
     *
     * @param ant number of ant
     */
    void countErrors(int ant) {
        ants[ant].pError = trainer.f_to_minimize(ants[ant].pVector);

        // local best solution
        if (ants[ant].pError < lBestError) {
            lBestError = ants[ant].pError;

            System.arraycopy(ants[ant].pVector, 0, lBestVector, 0, dimensions);
            // for (int i = 0; i < dimensions; i++)	lBestVector[i] = ants[ant].pVector[i];
            // global best solution
            if (ants[ant].pError < gBestError) {
                gBestError = ants[ant].pError;
                System.arraycopy(ants[ant].pVector, 0, gBestVector, 0, dimensions);
                //for (int i = 0; i < dimensions; i++)	gBestVector[i] = ants[ant].pVector[i];
                stagnation = 0;
            }
        }
    }

    /**
     * sort ants according to their fitness  (pError)
     */
    private void sortAnts() {
        for (int i = 0; i < populationSize + replace; i++) countErrors(i);
        Arrays.sort(ants);
    }

    /**
     * count sum of weights from all ants (params q, k - popluationSize)
     *
     * @return sum of weights for all ants
     */
    private double getWeightsSum() {
        // count sum of weights from all ants (params q, k - popluationSize)
        double sumWeights = 0.0;
        for (int i = 0; i < populationSize; i++) {
            ants[i].gWeight = ((1 / (q * populationSize * Math.sqrt(2 * Math.PI))) * Math.exp(-(i * i) / (2 * q * q * populationSize * populationSize)));
            sumWeights += ants[i].gWeight;
        }
        return sumWeights;
    }

    /**
     * modify solution using gradient information
     *
     * @param solution actual solution to modify by gradient
     */
    private void addGradient(double[] solution) {
        double[] gradient = new double[dimensions];
        trainer.gradient(solution, gradient);
        for (int d = 0; d < dimensions; d++) {
            solution[d] = solution[d] - gradientWeight * gradient[d];
        }
    }

    /**
     * choose one ant with probability proportional to its gWeight parameter
     *
     * @param sumWeights sum of weights for all ants
     * @return ant chosen ant
     */
    private int chooseAnt(double sumWeights) {
        double ran = generator.nextDouble() * sumWeights;
        double sum = 0.0;
        int chosen = 0;
        while (sum < ran) sum += ants[chosen++].gWeight;
        chosen--;

        return chosen;
    }

    /**
     * get deviation from mean for dimension i across whole population
     *
     * @param mean mean value from which the deviation is computed
     * @param i    dimension
     * @return deviation from mean across population
     */
    private double getDeviation(double mean, int i) {
        double deviation = 0.0;
        int nearestCount = 0;

        for (int n = 0; n < populationSize; n++) {
            double tmp = (ants[n].pVector[i] - mean);
            if (!standardDeviation) {
                if ((!forceDiversity) || (tmp < diversityLimit)) {
                    deviation += Math.abs(tmp);
                    nearestCount++;
                }
            } else {
                if ((!forceDiversity) || (tmp < diversityLimit)) {
                    deviation += (tmp * tmp);
                    nearestCount++;
                }
            }
        }
        if (!standardDeviation)
            deviation /= nearestCount;
        else {
            deviation /= (nearestCount - 1);
            deviation = Math.sqrt(deviation);
        }
        deviation *= r;

        return deviation;
    }

    /**
     * create new ants using probabilistic mixture of gaussian functions
     */
    private void createNewAnts() {
        double sumWeights = getWeightsSum();

        // create new ants
        for (int num = populationSize; num < populationSize + replace; num++) {
            // choose ant j* - chosen
            int chosen = chooseAnt(sumWeights);

            // for each dimension generate new mean value
            // according to mean and deviation
            double mean;
            double deviation;

            for (int i = 0; i < dimensions; i++) {
                mean = ants[chosen].pVector[i];
                deviation = getDeviation(mean, i);
                // sample Xi -> new mi & sigma
                double x = (generator.nextGaussian() * deviation) + mean;
                newAntPos[i] = x;
            }


            // pheromone & gradient heuristic
            if (gradientWeight != 0.0) addGradient(newAntPos);

            System.arraycopy(newAntPos, 0, ants[num].pVector, 0, dimensions);
        }

    }

    /**
     * main body of algorithm
     */
    public void run() {
        // display auxiliary window
        // if (graphicsOn) {   window.setColony(this); window.setVisible(true); }

        gBestError = Double.POSITIVE_INFINITY;
        lBestError = Double.POSITIVE_INFINITY;
        iteration = 1;
        stagnation = 0;

        sortAnts();
        // main cycle - repeat until maxIteration or maxStagnation
        do {
            // if (graphicsOn) {   window.p.setError(gBestError);   window.dopaint();   }
            createNewAnts();
            sortAnts();
            if (debugOn)
                System.out.println("Iteration: " + iteration + "; global best error: " + df.format(gBestError));
        } while ((stagnation++ < maxStagnation) && (iteration++ < maxIterations));

        // if (graphicsOn) window.setVisible(false);
    }

}
