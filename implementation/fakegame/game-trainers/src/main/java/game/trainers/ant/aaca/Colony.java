/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.3
 * <p>
 * <p>Title: Adaptive Ant Colony Optimization - colony</p>
 * <p>Description: class for AACA colony</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 * <p>
 * <p>Source: An adaptive ant colony system algorithm for continuous-space optimization problems.
 * Yan-jun L., Tie-jun W.
 * Journal of Zhejiang University SCIENCE, Vol. 4, No. 1, 2003.
 * http://www.zju.edu.cn/jzus/2003/0301/030107.pdf
 * </p>
 * <p>
 * <p>Algorithm: ACO applied to the binary representation of a variable. Ants build paths
 * from MSB to LSB deciding between 0 and 1 in each step. Amount of pheromone
 * is transformed using sigmoid curve to ensure faster convergence in
 * most signific bits.
 * <p>
 * 0---0---0---0---0
 * / \ / \ / \ / \ /
 * ---> S   X   X   X   X    ...
 * \ / \ / \ / \ / \
 * 1---1---1---1---1
 **/

package game.trainers.ant.aaca;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Random;

public class Colony {

    // window for visualisation
    // AACAWindow window;

    // parameters of algorithm 
    // double  minAcceptableError; // minimal acceptable error
    private int maxIterations;      // maximum of iterations
    private int maxStagnation;      // maximum of iterations without improvement

    private boolean debugOn;
    private boolean profilingOn;
    // boolean graphicsOn;         // show graphics window

    private int populationSize;     // number of ants in population
    int encodingLength;     // number of bits in searching variables
    private double evaporationFactor;  // lambda <0,1>
    private double pheromoneIndex;     // beta >= 0
    private double costIndex;          // delta >= 0
    private double min;                // parameter minimum
    private double max;                // parameter maximum

    private DecimalFormat df;
    private int iteration;              // current iteration
    private int stagnation;             // iterations without inprovement
    private Uncmin_methods trainer;
    int dimensions;             // number of variables to optimize

    private double gradientWeight;     // gradient heuristic impact

    private Random generator;

    public class pheromoneTable {
        double pheromoneFirst[][];
        double pheromoneOther[][][][];
        
        /* First Other
         *  |    /|\
         *  v   v v v
         *   0---0---0---0---0
         *  / \ / \ / \ / \ /
         * S   X   X   X   X
         *  \ / \ / \ / \ / \
         *   1---1---1---1---1
         */


        pheromoneTable() {
            pheromoneFirst = new double[dimensions][2];
            pheromoneOther = new double[dimensions][encodingLength - 1][2][2];
        }

        public void clear() {
            for (int d = 0; d < dimensions; d++) {
                pheromoneFirst[d][0] = 0.0;
                pheromoneFirst[d][1] = 0.0;
                for (int e = 0; e < encodingLength - 1; e++) {
                    pheromoneOther[d][e][0][0] = 0.0;
                    pheromoneOther[d][e][0][1] = 0.0;
                    pheromoneOther[d][e][1][0] = 0.0;
                    pheromoneOther[d][e][1][1] = 0.0;
                }
            }
        }

        public void init() {
            for (int d = 0; d < dimensions; d++) {
                pheromoneFirst[d][0] = 1.0;
                pheromoneFirst[d][1] = 1.0;
                for (int e = 0; e < encodingLength - 1; e++) {
                    pheromoneOther[d][e][0][0] = 1.0;
                    pheromoneOther[d][e][0][1] = 1.0;
                    pheromoneOther[d][e][1][0] = 1.0;
                    pheromoneOther[d][e][1][1] = 1.0;
                }
            }
        }

        public void setMinimum() {
            double minimum = 0.0001;

            for (int d = 0; d < dimensions; d++) {
                pheromoneFirst[d][0] = Math.max(pheromoneFirst[d][0], minimum);
                pheromoneFirst[d][1] = Math.max(pheromoneFirst[d][1], minimum);
                for (int e = 0; e < encodingLength - 1; e++) {
                    pheromoneOther[d][e][0][0] = Math.max(pheromoneOther[d][e][0][0], minimum);
                    pheromoneOther[d][e][0][1] = Math.max(pheromoneOther[d][e][0][1], minimum);
                    pheromoneOther[d][e][1][0] = Math.max(pheromoneOther[d][e][1][0], minimum);
                    pheromoneOther[d][e][1][1] = Math.max(pheromoneOther[d][e][1][1], minimum);
                }
            }
        }

        public void dump() {
            System.out.println("Colony dump:");
            for (int d = 0; d < dimensions; d++) {
                System.out.print(df.format(pheromoneFirst[d][0]) + " ");

                for (int e = 0; e < encodingLength - 1; e++)
                    System.out.print(df.format(pheromoneOther[d][e][0][0]) + " ");
                System.out.println();
                System.out.print("       ");
                for (int e = 0; e < encodingLength - 1; e++)
                    System.out.print(df.format(pheromoneOther[d][e][0][1]) + " ");
                System.out.println();

                System.out.print(df.format(pheromoneFirst[d][1]) + " ");
                for (int e = 0; e < encodingLength - 1; e++)
                    System.out.print(df.format(pheromoneOther[d][e][1][0]) + " ");
                System.out.println();
                System.out.print("       ");
                for (int e = 0; e < encodingLength - 1; e++)
                    System.out.print(df.format(pheromoneOther[d][e][1][1]) + " ");
                System.out.println();
            }
        }
    }

    public pheromoneTable pheromone;
    private pheromoneTable pheromoneAdd;

    private int[][] path;
    private double cost;
    double[] values;

    private double[] gBestVector; // globaly best variables vector
    public double gBestError;    // global error
    private double lBestError;    // local error

    private double[] sum;


    private long generate;
    private long evaluate;
    private long laypheromone;
    private long evaporate;


    /* profiling */
    private long startTime;
    private long endTime;


    /**
     * constructor - saves parameters
     *
     * @param train
     * @param dimensions
     * @param maxIterations
     * @param maxStagnation
     * @param debugOn
     * @param populationSize
     * @param encodingLength
     * @param evaporationFactor
     * @param pheromoneIndex
     * @param costIndex
     * @param min
     * @param max
     * @param gradientWeight
     */
    /* public Colony(Uncmin_methods train, int dimensions, int maxIterations,
            int maxStagnation, boolean debugOn, boolean graphicsOn,
            int populationSize,
            int encodingLength, double evaporationFactor, double pheromoneIndex,
            double costIndex, double min, double max, AACAWindow window) */
    public Colony(Uncmin_methods train, int dimensions, int maxIterations,
                  int maxStagnation, boolean debugOn, int populationSize,
                  int encodingLength, double evaporationFactor, double pheromoneIndex,
                  double costIndex, double min, double max, double gradientWeight) {

        // connect visualization window with colony
        // this.window = window;
        // if (window!=null) window.setColony(this);

        generator = new Random();

        // store parameters
        this.maxIterations = maxIterations;
        this.maxStagnation = maxStagnation;
        // this.minAcceptableError = minAcceptableError;

        this.populationSize = populationSize;
        this.encodingLength = encodingLength;
        this.evaporationFactor = evaporationFactor;
        this.pheromoneIndex = pheromoneIndex;
        this.costIndex = costIndex;
        this.min = min;
        this.max = max;

        this.dimensions = dimensions;
        this.debugOn = debugOn;
        // this.graphicsOn = graphicsOn;
        this.trainer = train;

        this.gradientWeight = gradientWeight;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(4);

        pheromone = new pheromoneTable();
        pheromoneAdd = new pheromoneTable();

        path = new int[dimensions][encodingLength];

        values = new double[dimensions];

        gBestVector = new double[dimensions];

        sum = new double[dimensions];

        profilingOn = false;
    }

    /**
     * main purpose for this is destoying window
     */
    public void destroy() {
        df = null;
        generator = null;
        pheromone = null;
    }

    /**
     * directions
     */
    void colonyDump() {
        pheromone.dump();
        //pheromoneAdd.dump();
    }

    /**
     * main body of algorithm
     */
    public void run() {
        double error = Double.POSITIVE_INFINITY;

        gBestError = Double.MAX_VALUE;
        lBestError = Double.MAX_VALUE;

        pheromone.init();

        iteration = 1;
        stagnation = 0;
        // main cycle
        do {

            // ant cycle
            pheromoneAdd.clear();
            gBestError = lBestError;
            for (int d = 0; d < dimensions; d++) sum[d] = 0.0;
            for (int ant = 0; ant < populationSize; ant++) {
                generate();
                evaluate();
                layPheromone();
            }
            evaporate();

            // if (graphicsOn && (window!=null)) window.dopaint();

            if (gBestError < error) {
                stagnation = 0;
                error = gBestError;
            } else {
                stagnation++;
            }

            if (debugOn) System.out.println("Iteration: " + iteration + "; error: " + df.format(error));
            if (debugOn) colonyDump();

            if (profilingOn)
                System.out.println("Profiling: generate: " + generate + ", evaluate: " + evaluate + ", laypheromone: " + laypheromone + ", evaporate: " + evaporate);

        } while ((stagnation < maxStagnation) && (iteration++ < maxIterations));

    }

    /* generate new path stochastically from pheromone */
    private void generate() {
        if (profilingOn) startTime = System.currentTimeMillis();

        double total, sum, ran;
        int last, newlast;

        for (int d = 0; d < dimensions; d++) {
            total = pheromone.pheromoneFirst[d][0] + pheromone.pheromoneFirst[d][1];
            newlast = 0;
            last = 0;
            ran = Math.random() * total;
            if (pheromone.pheromoneFirst[d][0] < ran) {
                newlast++;
            }
            last = newlast;
            path[d][0] = last;
            for (int i = 0; i < encodingLength - 1; i++) {
                total = 0.0;
                for (int j = 0; j < 2; j++) total += pheromone.pheromoneOther[d][i][last][j];
                newlast = 0;
                sum = 0.0;
                ran = Math.random() * total;
                if (pheromone.pheromoneOther[d][i][last][0] < ran) {
                    newlast++;
                    sum += pheromone.pheromoneOther[d][i][last][newlast];
                }
                last = newlast;
                path[d][i + 1] = last;
            }
        }
        if (profilingOn) {
            endTime = System.currentTimeMillis();
            generate += endTime - startTime;
        }
    }

    /* compute cost of the given path */
    private void evaluate() {
        if (profilingOn) startTime = System.currentTimeMillis();
        for (int d = 0; d < dimensions; d++) {
            double x = 0;
            int power = 1;
            for (int bit = encodingLength - 1; bit >= 0; bit--) {
                x += path[d][bit] * power;
                power *= 2;
            }

            values[d] = (x / (power - 1)) * ((max - min) + min);
        }

        // pheromone & gradient heuristic
        if (gradientWeight != 0.0) addGradient(values);

        if (profilingOn) {
            endTime = System.currentTimeMillis();
            evaluate += endTime - startTime;
        }
        cost = trainer.f_to_minimize(values);
        if (cost <= gBestError) {
            lBestError = cost;
            gBestVector = values;
        }
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

    /* lay pheromone on the path, amount depends on the cost*/
    private void layPheromone() {
        if (profilingOn) startTime = System.currentTimeMillis();
        double add;
        for (int d = 0; d < dimensions; d++) {
            add = 5 / (1 + Math.exp(pheromoneIndex * (encodingLength + 1) * (((cost - gBestError) / (gBestError / 100)) - costIndex)));
            pheromoneAdd.pheromoneFirst[d][path[d][0]] += add;
            sum[d] += add;

            for (int bit = 1; bit < encodingLength; bit++) {
                add = 5 / (1 + Math.exp(pheromoneIndex * (encodingLength + 1 - bit) * (((cost - gBestError) / (gBestError / 100)) - costIndex)));
                pheromoneAdd.pheromoneOther[d][bit - 1][path[d][bit - 1]][path[d][bit]] += add;
                sum[d] += add;
            }

        }
        if (profilingOn) {
            endTime = System.currentTimeMillis();
            laypheromone += endTime - startTime;
        }
    }

    /* evaporate pheromone*/
    private void evaporate() {
        if (profilingOn) startTime = System.currentTimeMillis();
        /* pheromone addition */
        for (int d = 0; d < dimensions; d++) {
            if (sum[d] > 0) {
                pheromone.pheromoneFirst[d][0] += pheromoneAdd.pheromoneFirst[d][0] / (sum[d] * 2);
                pheromone.pheromoneFirst[d][1] += pheromoneAdd.pheromoneFirst[d][1] / (sum[d] * 2);
                for (int bit = 0; bit < encodingLength - 1; bit++) {
                    pheromone.pheromoneOther[d][bit][0][0] += pheromoneAdd.pheromoneOther[d][bit][0][0] / sum[d];
                    pheromone.pheromoneOther[d][bit][0][1] += pheromoneAdd.pheromoneOther[d][bit][0][1] / sum[d];
                    pheromone.pheromoneOther[d][bit][1][0] += pheromoneAdd.pheromoneOther[d][bit][1][0] / sum[d];
                    pheromone.pheromoneOther[d][bit][1][1] += pheromoneAdd.pheromoneOther[d][bit][1][1] / sum[d];
                }
            }
        }
        
        /* phromone evaporation */
        for (int d = 0; d < dimensions; d++) {
            pheromone.pheromoneFirst[d][0] =
                    evaporationFactor * pheromone.pheromoneFirst[d][0] +
                            (1 - evaporationFactor) * pheromoneAdd.pheromoneFirst[d][0];
            pheromone.pheromoneFirst[d][1] =
                    evaporationFactor * pheromone.pheromoneFirst[d][1] +
                            (1 - evaporationFactor) * pheromoneAdd.pheromoneFirst[d][1];
            for (int bit = 0; bit < encodingLength - 1; bit++) {
                pheromone.pheromoneOther[d][bit][0][0] =
                        evaporationFactor * pheromone.pheromoneOther[d][bit][0][0] +
                                (1 - evaporationFactor) * pheromoneAdd.pheromoneOther[d][bit][0][0];
                pheromone.pheromoneOther[d][bit][0][1] =
                        evaporationFactor * pheromone.pheromoneOther[d][bit][0][1] +
                                (1 - evaporationFactor) * pheromoneAdd.pheromoneOther[d][bit][0][1];
                pheromone.pheromoneOther[d][bit][1][0] =
                        evaporationFactor * pheromone.pheromoneOther[d][bit][1][0] +
                                (1 - evaporationFactor) * pheromoneAdd.pheromoneOther[d][bit][1][0];
                pheromone.pheromoneOther[d][bit][1][1] =
                        evaporationFactor * pheromone.pheromoneOther[d][bit][1][1] +
                                (1 - evaporationFactor) * pheromoneAdd.pheromoneOther[d][bit][1][1];
            }
        }

        pheromone.setMinimum();

        if (profilingOn) {
            endTime = System.currentTimeMillis();
            evaporate += endTime - startTime;
        }
    }

    /** returns global best solution */
    // public double getBest(int i) { return gBestVector[i]; }

}
