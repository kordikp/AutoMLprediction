package game.trainers.pso;

import game.trainers.gradient.Newton.Uncmin_methods;

public class HParticle {
    //   --staticke promenne
    static private int nDimensions; //pocet prvku ve vektoru

    static private double dimensionSize; // velikost rozmeru

    static private double gBest[]; //nejlepsi pozice ze vsech ptaku v historii

    static private double gBestError; //chyba z nelepsi pozice

    static private double c1;

    static private double c2;

    static private double maxVelocityComponent;

    static private int gAge;

    private static Uncmin_methods method;

    //   --instancni promenne
    private double pBest[]; //nejlepsi pozice ptaka v historii

    private double pBestError; //vyska = chyba u nejlepsi pozice ptaka

    public double position[]; //pozice ptaka

    private double positionError;

    private double velocity[]; // rychlost ptaka

    private int age; // vek ptaka

    public static void firstInit(double c1, double c2, int dimensions, double dimSize, double max_vel, Uncmin_methods met) {
        HParticle.nDimensions = dimensions;
        HParticle.dimensionSize = dimSize;
        HParticle.c1 = c1;
        HParticle.c2 = c2;
        HParticle.maxVelocityComponent = max_vel;
        HParticle.gAge = 1;
        HParticle.method = met;
        gBest = new double[dimensions];
        gBestError = Double.POSITIVE_INFINITY;
    }

    public HParticle() {
        position = new double[nDimensions];
        pBest = new double[nDimensions];
        velocity = new double[nDimensions];
    }

    public void init() {
        //vygenerovani nahodne pozice ptaka v rozmezi +-dimensionSize/2
        //inicializovana pozice je zaroven nejlepsi dosazenou
        for (int i = 0; i < nDimensions; i++) {
            pBest[i] = position[i] = (Math.random() * dimensionSize) - dimensionSize / 2.0;
            velocity[i] = 0; //(Math.random() * maxVelocityComponent) -
            // maxVelocityComponent / 2.0;
        }
        pBestError = positionError = Double.POSITIVE_INFINITY;
        age = 1;
    }

    public HParticle createCopy() {
        HParticle newParticle = new HParticle();
        for (int i = 0; i < nDimensions; i++) {
            newParticle.position[i] = position[i];
            newParticle.pBest[i] = pBest[i];
            newParticle.velocity[i] = 0;
        }
        newParticle.positionError = positionError;
        newParticle.pBestError = pBestError;
        newParticle.age = age;
        return newParticle;
    }

    public HParticle createCopyBasic() {
        HParticle newParticle = new HParticle();
        System.arraycopy(position, 0, newParticle.position, 0, nDimensions);
        newParticle.age = age;
        return newParticle;
    }

    public void initAfterCrossover() {
        for (int i = 0; i < nDimensions; i++) {
            pBest[i] = position[i];
            velocity[i] = 0; // (Math.random() * maxVelocityComponent) -
            // maxVelocityComponent / 2.0;
        }
        pBestError = positionError = Double.POSITIVE_INFINITY;
        age = 1;
    }

    private double getError() {
        if (method == null) return fitness1();
        else return method.f_to_minimize(position);
        //        return fitness1();
        //        return fitness2();
    }

    private double fitness1() {
        double result = 0;
        for (double aPosition : position) {
            result += Math.pow(aPosition, 2.0);
            /*
             * switch (i % 3) { case 0: result += Math.pow(position[i] - 0.25,
             * 2.0); break; case 1: result += Math.pow(position[i] + 0.25, 2.0);
             * break; case 2: result += Math.pow(position[i] - 0.1, 2.0); break; }
             */
        }
        result = Math.sqrt(result);
        if (result <= dimensionSize / 4.0) return 1 / result - 4.0;
        else return result * Math.pow(4 / dimensionSize, 2) - 4.0;
    }

    private double fitness2() {
        double result = 0;
        for (double aPosition : position) result += Math.pow(aPosition - 0.25, 2.0);
        return Math.sqrt(result);
    }

    public void calculateErrors() {
        positionError = getError();
        if (positionError < pBestError) {
            copyPosition(pBest, position);
            pBestError = positionError;
            if (pBestError < gBestError) {
                copyPosition(gBest, pBest);
                gBestError = pBestError;

                //    System.out.println("Best global solution: error = " +  gBestError);
            }
        }
    }

    public void newVelocityAndPosition() {
        for (int component = 0; component < nDimensions; component++) {
            double phi1 = Math.random();
            double phi2 = Math.random();
            velocity[component] += c1 * phi1 * (pBest[component] - position[component]) + c2 * phi2 * (gBest[component] - position[component]);
            trimVelocityComponent(component);
            position[component] += velocity[component];
        }
    }

    public void trimVelocity() {
        for (int i = 0; i < nDimensions; i++)
            trimVelocityComponent(i);
    }

    void trimVelocityComponent(int component) {
        velocity[component] = Math.min(Math.max(velocity[component], -maxVelocityComponent), maxVelocityComponent);
    }

    void copyPosition(double[] to, double[] from) {
        System.arraycopy(from, 0, to, 0, nDimensions);
    }

    public void mutate(int dimension) {
        // the new (mutated) value is drawn uniformly from the interval [-2x,
        // 2x),
        //		where x is the current value
        position[dimension] = (Math.random() * 4 - 2) * position[dimension];
    }

    public int incrementAge() {
        return ++age;
    }

    public int getAge() {
        return age;
    }

    public static int getgAge() {
        return gAge;
    }

    public static void setgAge(int a) {
        HParticle.gAge = a;
    }

    public static double getgBest(int i) {
        return gBest[i];
    }

    public static double getgBestError() {
        return gBestError;
    }

    public double getpBest(int i) {
        return pBest[i];
    }

    public void setpBest(int i, double value) {
        pBest[i] = value;
    }

    public double getpBestError() {
        return pBestError;
    }

    public double getPosition(int i) {
        return position[i];
    }

    public void setPosition(int i, double value) {
        position[i] = value;
    }

    public double getPositionError() {
        return positionError;
    }
}
