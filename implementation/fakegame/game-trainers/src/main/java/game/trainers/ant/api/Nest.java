/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: API - nest</p>
 * <p>Description: class for API nest</p>
 * <p>Hybridized with gradient, multi-core ready</p>
 * <p>
 * <p>Source: On how Pachycondyla apicalis ants suggest a new search algorithm
 * N. Monmarche, G. Venturini, M. Slimane
 * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.20.8045
 * </p>
 */

package game.trainers.ant.api;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.text.DecimalFormat;
import java.util.Random;

public class Nest {

    // parameters of algorithm
    private int antsCount;              // number of ants
    int huntingPlaces;          // hunting places count for one ant
    private int moveGeneration;         // generation befor moving nest
    private double minAcceptableError;  // minimal acceptable error
    private int maxIterations;          // maximum of iterations

    private boolean debugOn;

    private DecimalFormat df;
    private int iteration = 1;              // current iteration
    private Uncmin_methods trainer;
    private int dimensions;                 // number of variables to optimize

    double position[];              // nest coordinates
    private double[] best;                  // best solution
    private double bestFitness;             // nest fitness (best solution)

    private Ant[] ants;            // directions (positions, pheromone)

    private Random generator;

    private double gradientWeight;          // gradient heuristic impact


    public void compareGlobal(double fitness, double[] position) {
        if (fitness < bestFitness) {
            bestFitness = fitness;
            System.arraycopy(position, 0, best, 0, dimensions);
        }
    }

    /**
     * constructor - saves parameters
     *
     * @param train
     * @param dimensions
     * @param antsCount
     * @param huntingSites
     * @param moveGeneration
     * @param starvation
     * @param maxIterations
     * @param minAcceptableError
     * @param debugOn
     * @param gradientWeight
     */
    public Nest(Uncmin_methods train, int dimensions, int antsCount, int huntingSites,
                int moveGeneration, int starvation, int maxIterations, double minAcceptableError,
                boolean debugOn, double gradientWeight) {

        generator = new Random();

        this.antsCount = antsCount;
        this.moveGeneration = moveGeneration;
        this.maxIterations = maxIterations;
        this.minAcceptableError = minAcceptableError;

        this.dimensions = dimensions;
        this.debugOn = debugOn;
        trainer = train;

        this.gradientWeight = gradientWeight;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(4);


        // nest position
        position = new double[dimensions];

        best = new double[dimensions];

        //Ant.firstInit(trainer, dimensions, antsCount, starvation);

        ants = new Ant[antsCount];
        for (int i = 0; i < antsCount; i++)
            ants[i] = new Ant(i, dimensions, huntingSites, antsCount, this, trainer, starvation);

    }

    public void colonyDump() {
        System.out.print("Nest: ");
        for (int i = 0; i < dimensions; i++) System.out.print(df.format(position[i]) + " ");
        System.out.println();

        /*  for (int i=0; i<antsCount; i++) {
                ants[i].println();
            } */
        System.out.println();
    }

    /**
     * main body of algorithm
     */
    public void run() {
        //double solution[];
        double error;

        // place nest
        for (int i = 0; i < dimensions; i++) {
            position[i] = (Math.random() * 20.0) - 10.0;
            best[i] = position[i];
        }
        bestFitness = trainer.f_to_minimize(best);


        // for all ants
        do {
            for (int ant = 0; ant < antsCount; ant++) {

                ants[ant].checkHuntingSitesQueue();

                if (ants[ant].huntingSiteAdded()) {
                    ants[ant].explore(ants[ant].lastCreatedHS());
                } else {
                    if (ants[ant].lastSearchSuccessful()) {
                        ants[ant].explore(ants[ant].lastVisitedHS());
                    } else {
                        ants[ant].explore(ants[ant].getRandomHS());
                    }
                }


            }

            tandemRun();

            if (iteration % moveGeneration == 0)
                moveNest();

            error = bestFitness;

            if (debugOn) System.out.println("Iteration: " + iteration + "; error: " + df.format(error));
            //if (debugOn) colonyDump();

        } while ((iteration++ < maxIterations) && (error > minAcceptableError));

    }

    private void tandemRun() {
        /* 2 ants, copy of the best hunting site */
        int ant1, ant2;

        ant1 = generator.nextInt(antsCount);
        ant2 = generator.nextInt(antsCount);
        if (ant1 != ant2) {
            if (ants[ant1].getFitness() > ants[ant2].getFitness()) {
                int tmp = ant1;
                ant1 = ant2;
                ant2 = tmp;
            }

            ants[ant1].tandemRun(ants[ant2]);
        }
    }

    private void moveNest() {
        System.arraycopy(best, 0, position, 0, dimensions);

        for (int ant = 0; ant < antsCount; ant++) {
            ants[ant].forgetAll();
        }

        if (debugOn) System.out.println("Moving nest.");
    }
}
