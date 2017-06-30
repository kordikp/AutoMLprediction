package game.trainers.genetic;

import game.trainers.gradient.numopt.LineSearch;
import game.trainers.gradient.numopt.LineSearchException;
import game.trainers.gradient.numopt.LineSearchFactory;
import game.trainers.gradient.numopt.SimpleStopCondition2;
import game.trainers.gradient.numopt.StopCondition;

import common.MachineAccuracy;
import common.RND;
import common.function.NumericalDifferentiation;
import common.function.ObjectiveFunction;

/**
 * User: drchaj1
 * Date: 2.5.2007
 * Time: 21:27:36
 */

/**
 * Unconstrained version with gradient algorithms. No constraints. Turned to minimization!.
 * <p/>
 * <p/>
 * The <b>Gartou</b> class implements the SADE genetic algorithm. The SADE operates
 * on real domains (the chromosomes are represented by real number vectors).
 * The scheme of SADE:
 * <pre>
 * void SADE ( void )
 * {
 *   firstGeneration ();
 *    while ( to_be_continued )
 *    {
 *      mutate ();
 *      localMutate ();
 *      crossDE ();
 *      evaluatePopulation ();
 *      select ();
 *    }
 * }
 * </pre>
 * <p/>
 * Tournament selection is used. The best half of population is preserved to the next generation.
 */

public class DGartou {
    /**
     * Dimensions to optimize.
     */
    private int n;
    /**
     * Determines the size of the pool.
     */
    private int poolRate = 10;
    /**
     * The probability of the MUTATION.
     */
//    public double radioactivity = 0.1;
    private double radioactivity = 0.0;

    //    public double gradientLineSearchRadioactivity = 0.1;
    private double gradientRadioactivity = 0.3;
    /**
     * The probability of the LOCAL_MUTATION.
     */
//    public double localRadioactivity = 0.1;
    private double localRadioactivity = 0.1;
    /**
     * Determines the amount of MUTATION.
     */
    private double mutationRate = 0.5;
    /**
     * Determines the amount of LOCAL_MUTATION.
     */
    private double mutagenRate = 400;
    /**
     * Amount of the crossDE.
     */
    private double deRate = 0.3;

    private double minInitConstraint = -10.0;
    private double maxInitConstraint = 10.0;
    private double maxInitMutagen = 0.0; //TODO remove for simplicity

    /**
     * Optimization process will stop when the number of weakness function calls exceeds this value.
     */
    public int fitnessCallsLimit;

    private ObjectiveFunction func;

    private int actualSize;
    private int fitnessCall;
    private int poolSize;
    private int selectedSize;
    private int generation;

    //we are minimizing thus we call use "weakness" instead of fitness
    private double[] weakness;
    private double[] mutagen;
    private double[][] genome;

    private double[] bsf, btg;
    private double bsfValue, btgValue;

    //storage for gradient
    private double[] grad;
    //direction
    private double[] dir;

    //line search
    private LineSearch lineSearch;

    private StopCondition stopCondition;

    /**
     * Constructor for <b>Gartou</b> object. The parameter represents the optimized function.
     *
     * @param ofunc function to optimize
     */
    public DGartou(ObjectiveFunction ofunc) {
        this(ofunc, LineSearchFactory.createDefault(ofunc));
    }

    private DGartou(ObjectiveFunction ofunc, LineSearch olineSearch) {
        func = ofunc;
        n = func.getNumArguments();
        lineSearch = olineSearch;
        bsfValue = Double.POSITIVE_INFINITY;
        btgValue = Double.POSITIVE_INFINITY;
        stopCondition = new SimpleStopCondition2(MachineAccuracy.SQRT_EPSILON, 15);
    }

//---------- SADE Technology ---------------------------------------------------

    private double[] newPoint() // creates new random point
    {
        double[] x = new double[func.getNumArguments()];
        for (int i = 0; i < x.length; i++) {
            x[i] = RND.getDouble(minInitConstraint, maxInitConstraint);
        }
        return x;
    }

    private void configuration() {
        poolSize = 2 * poolRate * n;
        selectedSize = poolRate * n;
        mutagen = new double[n];
        double size = maxInitConstraint - minInitConstraint;
        for (int i = 0; i < n; i++) {
            mutagen[i] = size / mutagenRate;
            if (maxInitMutagen > mutagen[i]) mutagen[i] = maxInitMutagen;
        }

        grad = new double[n];
        dir = new double[n];
    }

    private void firstGeneration() {
        generation = 1;
        weakness = new double[poolSize];
        genome = new double[poolSize][];
        for (int i = 0; i < poolSize; i++) {
            genome[i] = newPoint();
        }
        bsf = new double[n];
        actualSize = poolSize;
        evaluatePopulation(0);
        select();
    }

    private void evaluatePopulation(int ostart) {
        for (int i = ostart * selectedSize; i < actualSize; i++) {

            weakness[i] = func.evaluate(genome[i]);
            fitnessCall++;
            if (weakness[i] < btgValue) {
                btgValue = weakness[i];
                btg = genome[i];
            }
        }
        if (btgValue < bsfValue) {
            bsf = btg.clone();
            bsfValue = btgValue;
            int bsfBirth = fitnessCall;
        }
    }

    private void select() {
        double[] h;
        int i1, i2, dead, last;

        //tournament
        while (actualSize > selectedSize) {
            //choose two random organisms
            i1 = RND.getInt(0, actualSize - 1);
            i2 = RND.getInt(1, actualSize - 1);
            //not the same twice
            if (i1 == i2) {
                i2--;
            }
            //higher weakness dies
            if (weakness[i1] < weakness[i2]) {
                dead = i2;
            } else {
                dead = i1;
            }
            //the dead one will not undergo selection again
            last = actualSize - 1;

            h = genome[last];
            genome[last] = genome[dead];
            genome[dead] = h;

            if (btg == genome[last]) {
                btg = genome[dead];
            }

            weakness[dead] = weakness[last];
            actualSize--;
        }
    }

    private void mutate() {
        double p;
        double[] x;
        int index;

        for (int i = 0; i < selectedSize; i++) {
            if (actualSize == poolSize) {
                break;
            }
            p = RND.getDouble(0, 1);
            if (p <= radioactivity) {
                index = RND.getInt(0, selectedSize - 1);
                mutationRate = RND.getDouble(0, 1);
                x = newPoint();
                for (int j = 0; j < n; j++) {
                    genome[actualSize][j] = genome[index][j] + mutationRate * (x[j] - genome[index][j]);
                }
                actualSize++;
            }
        }
    }

    private void localMutate() {
        double p, delta;
        int index;
        for (int i = 0; i < selectedSize; i++) {
            if (actualSize == poolSize) {
                break;
            }
            p = RND.getDouble(0, 1);
            if (p <= localRadioactivity) {
                index = RND.getInt(0, selectedSize - 1);

                for (int j = 0; j < n; j++) {
                    delta = RND.getDouble(-mutagen[j], mutagen[j]);
                    genome[actualSize][j] = genome[index][j] + delta;
                }
                actualSize++;
            }
        }
    }

    private void gradientLineSearchMutate() {
        double p;
        double[] x;
        int index;

        for (int i = 0; i < selectedSize; i++) {
            if (actualSize == poolSize) {
                break;
            }
            p = RND.getDouble(0, 1);
            if (p <= gradientRadioactivity) {
                index = RND.getInt(0, selectedSize - 1);

                x = genome[index].clone();
                //evaluate gradient - the direction of the steepest ascent
                double fx;
                if (func.isAnalyticGradient()) {
                    //computes analytic gradient
                    fx = func.evaluate(x, grad);
                } else {
                    //computes numerical gradient
                    fx = func.evaluate(x);
                    NumericalDifferentiation.gradientCD(func, x, grad);
                }
                //steepest descent is negatve
                dir = grad.clone();
                for (int ii = 0; ii < dir.length; ii++) {
                    dir[ii] = -dir[ii];
                }


                try {
//                    System.out.print("fx_old = " + weakness[index]);
                    fx = lineSearch.minimize(x, dir, fx, grad);//TODO make better (used evaluated function)
//                    System.out.println(" fx = " + fx);
                } catch (LineSearchException e) {
//                    e.printStackTrace();
                }
                genome[actualSize] = x;

                actualSize++;
            }
        }
    }

    private void crossDE() {
        int i1, i2, i3;
        while (actualSize < poolSize) {
//            System.out.println("crossDE");
            i1 = RND.getInt(0, selectedSize - 1);
            i2 = RND.getInt(1, selectedSize - 1);
            if (i1 == i2) {
                i2--;
            }
            i3 = RND.getInt(0, selectedSize - 1);
            for (int j = 0; j < n; j++) {
                genome[actualSize][j] = genome[i3][j] + deRate * (genome[i2][j] - genome[i1][j]);
            }
            actualSize++;
        }
    }

    private boolean toContinue() {
        boolean cont = true;
        if (generation == 1) {
            stopCondition.init(btgValue);
        } else {
            cont = !stopCondition.stop(btgValue);
        }
        if (fitnessCall > fitnessCallsLimit) {
            cont = false;
        }
        //if ( func.optimum !=  null ) //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //if ( func.optimum-bsfValue <= func.precision ) return false;
        return cont;
    }

//---------- END SADE Technology -----------------------------------------------

    /**
     * Returns the value of the "best so far" chromosome.
     *
     * @return BSF value
     */
    public double getBsfValue() {
        return bsfValue;
    }

    /**
     * Returns the "best so far" chromosome.
     *
     * @return BSF
     */
    public double[] getBsf() {
        return bsf;
    }

    /**
     * Returns the value of the "best this generation" chromosome.
     *
     * @return BTG value
     */
    public double getBtgValue() {
        return btgValue;
    }

    /**
     * Returns the "best this generation" chromosome.
     *
     * @return BTG
     */
    public double[] getBtg() {
        return btg;
    }

    /**
     * Returns number of chromosomes in pool.
     *
     * @return number of chromosomes
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Returns the array of chromosomes.
     *
     * @return array of chromosomes (array of <b>double</b> vectors)
     */
    public double[][] getGenome() {
        return genome;
    }

    /**
     * Prints results during the optimization process. Redefine this method to visualise the optimization.
     */
    public void printNews() {
        System.out.print("fc=" + fitnessCall +
                " BTG=" + btgValue +
                " BSF=" + bsfValue + " ");
    }

    /**
     * Runs the optimization process.
     */
    public void run() {
        boolean cont;
        configuration();
        firstGeneration();
        cont = toContinue();
        while (cont) {
            mutate();
            gradientLineSearchMutate();
            localMutate();
            crossDE();
            evaluatePopulation(1);
            select();

//            printNews();
            cont = toContinue();
            generation++;
        }
//        func.evaluate(bsf); //
    }

}
