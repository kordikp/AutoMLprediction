/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.1
 * <p>
 * <p>Title: Continuous Ant Colony Optimization (CACO) - Direction</p>
 * <p>Description: class for CACO direction</p>
 */

package game.trainers.ant.caco;

import game.trainers.gradient.Newton.Uncmin_methods;

class Direction {
    private static final int NO_IMPROVEMENT = 0;
    private static final int LOCAL_IMPROVEMENT = 1;
    private static final int GLOBAL_IMPROVEMENT = 1;

    private int dimensions;    // vector of variables size
    private double[] gBestVector; // globaly best variables vector
    private double gBestError;    // its error

    private double startingPheromone;
    private double minimumPheromone;
    private double addPheromone;
    private double evaporation;

    private Uncmin_methods trainer;

    // main
    private int lastErrorCheck;    // result of the last error check
    // (NO_IMPROVEMENT, LOCAL_IMPROVEMENT, GLOBAL_IMPROVEMENT)

    private double lBestError;    // local best error
    private double lBestVector[]; // local best position
    private double pError;        // present error
    private double[] pVector;     // present vector
    private double pPheromone;    // present pheromone for vector
    private double pPheromoneSum;
    private double gradientWeight;  // weight of gradient heusistic impact (0.0 = none)

    public Direction(double startingPheromone, double minimumPheromone,
                     double addPheromone, double evaporation, int dimensions, Uncmin_methods train,
                     double gradientWeight) {

        this.startingPheromone = startingPheromone;
        this.minimumPheromone = minimumPheromone;
        this.addPheromone = addPheromone;
        this.evaporation = evaporation;

        pVector = new double[dimensions];
        lBestVector = new double[dimensions];

        changePheromone(startingPheromone);
        pPheromoneSum += pPheromone;

        for (int i = 0; i < dimensions; i++) {
            pVector[i] = (Math.random() * 20.0) - 10.0; // random init (-10, 10)
            lBestVector[i] = pVector[i];                // best
        }

        lBestError = Double.POSITIVE_INFINITY;
        pError = Double.POSITIVE_INFINITY;
        lastErrorCheck = NO_IMPROVEMENT;

        this.dimensions = dimensions;
        trainer = train;
        gBestVector = new double[dimensions];
        gBestError = Double.POSITIVE_INFINITY;

        this.gradientWeight = gradientWeight;
    }

    /**
     * returns best error for this direction
     */
    public double getgBestError() {
        return gBestError;
    }

    /**
     * set best error for this direction
     *
     * @param err
     */
    public void setgBestError(double err) {
        gBestError = err;
    }

    /**
     * returns best vector for this direction
     */
    public double[] getgBestVector() {
        return gBestVector;
    }

    public double getgBest(int i) {
        return gBestVector[i];
    }

    public double getpVector(int i) {
        return pVector[i];
    }

    public void setpVector(int i, double x) {
        pVector[i] = x;
    }

    public double getPheromone() {
        return pPheromone;
    }


    void changePheromone(double amount) {
        if ((pPheromone + amount) < minimumPheromone) {
            amount = pPheromone - minimumPheromone; // ensures minimal pheromone value
        }
        pPheromone += amount;
        pPheromoneSum += amount;
    }

    private double getError(double[] present) {
        return trainer.f_to_minimize(present);
    }

    public void evaporatePheromone() {
        changePheromone(-evaporation * pPheromone);
    }

    public void explore(double radius) {

        double oldVector[] = new double[dimensions];

        // backup
        System.arraycopy(pVector, 0, oldVector, 0, dimensions);

        if (pPheromone == minimumPheromone) {
            for (int i = 0; i < dimensions; i++)
                pVector[i] = (Math.random() * 20.0) - 10.0;    // random init (-10, 10)
            // System.out.println("Random search");
        } else if (dimensions > 1) {
            // coount shift vector using n-dimensional spherical coordinates

            double vector[] = new double[dimensions];
            double angles[] = new double[dimensions - 1];
            double distance = Math.random() * radius;

            // angles
            for (int i = 0; i < dimensions - 2; i++) angles[i] = Math.random() * Math.PI;
            angles[dimensions - 2] = Math.random() * (Math.PI * 2);

            // vector <0, dimensions-2>
            for (int i = 0; i < dimensions - 1; i++) {
                vector[i] = distance;
                for (int j = 0; j < i; j++)
                    vector[i] *= Math.sin(angles[j]);
                vector[i] *= Math.cos(angles[i]);
            }

            // vector [dimensions-1]
            vector[dimensions - 1] = distance;
            for (int j = 0; j < dimensions - 1; j++)
                vector[dimensions - 1] *= Math.sin(angles[j]);

            // add to pVector
            for (int i = 0; i < dimensions; i++)
                pVector[i] += vector[i];

        } else {
            // for 1D
            pVector[0] += (Math.random() * radius * 2) - radius;
        }

        if (gradientWeight > 0.0) addGradient(pVector);


        countErrors();

        if (lastErrorCheck == NO_IMPROVEMENT) {
            System.arraycopy(oldVector, 0, pVector, 0, dimensions);
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

    /**
     * counts error for current ants vector error
     */
    void countErrors() {
        pError = getError(pVector);
        if (pError < lBestError) { // local
            lBestError = pError;
            System.arraycopy(pVector, 0, lBestVector, 0, dimensions);
            if (pError < gBestError) { // global
                gBestError = pError;
                System.arraycopy(pVector, 0, gBestVector, 0, dimensions);
                lastErrorCheck = GLOBAL_IMPROVEMENT;
                changePheromone(addPheromone);
                // System.out.println("Global improvement");
            } else {
                lastErrorCheck = LOCAL_IMPROVEMENT;
                changePheromone(addPheromone);
                // System.out.println("Local improvement");
            }

        } else {
            lastErrorCheck = NO_IMPROVEMENT;
        }

    }
}
