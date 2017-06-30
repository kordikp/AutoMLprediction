package game.trainers.pso;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.util.Random;

public class HSwarm {
    private int nDimensions = 5;

    private double dimensionSize = 1;

    private int nParticles = 100;

    private int maxGenerations = 100;

    private double minAcceptableError = 1e-10;

    private double maxVelocityComponent = 0.00001;

    private double eliteRatio = 0.5;

    private HParticle[] particle;

    private Uncmin_methods method;

    private Random myRandom;

    private HWindow window;

    public static void main(String[] args) {
        HSwarm h = new HSwarm(null, 5, 1.0, 200, 1000000, 1e-10, 1e-5, 0.8);
        h.run(2.0, 2.0);
    }

    public HSwarm(Uncmin_methods met, int dims) {
        method = met;
        nDimensions = dims;
        particle = new HParticle[nParticles];
        myRandom = new Random(Math.max(nParticles, nDimensions));
    }

    private HSwarm(Uncmin_methods met, int dims, double dimSize, int parts, int gens, double error, double maxVel, double eRatio) {
        method = met;
        nDimensions = dims;
        dimensionSize = dimSize;
        nParticles = parts;
        maxGenerations = gens;
        minAcceptableError = error;
        particle = new HParticle[nParticles];
        myRandom = new Random(Math.max(nParticles, nDimensions));
        maxVelocityComponent = maxVel;
        eliteRatio = eRatio;
    }

    public void run(double c1, double c2) {
        int generation;
        int lastBestErrorDecrease = 0;
        double lastBestError;
        HParticle.firstInit(c1, c2, nDimensions, dimensionSize, maxVelocityComponent, method);
        createPopulation(); // create a new population of particles

        /* if (nDimensions >= 2) {
            window = new HWindow(nDimensions, dimensionSize, particle);
            window.setVisible(true);
        }*/
        lastBestError = HParticle.getgBestError();
        for (generation = 0; generation < maxGenerations && (lastBestError > minAcceptableError); generation++) {
            calculateErrors(nParticles); // calculate error for each
            // particle's position
            int nElites = extractElites(eliteRatio);
            // sort elites according to their error
            // discard the worst ones
            enhanceElites(nElites, 1); // enhance the elites using PSO
            ageElites(nElites);
            completePopulation(nElites);
            // VYTVOR NOVYCH N/2 PTAKU TURNAJOVYM VYBEREM A GA-TMEM

            /*          try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted:" + e);
            }*/
            if (HParticle.getgBestError() < lastBestError) {
                lastBestError = HParticle.getgBestError();
                lastBestErrorDecrease = generation;
            }
            if ((generation - lastBestErrorDecrease) > 20) break;
            // stop if error is not decreasing for 20 generations
        }
        sortParticles(1);
        HShowComponent.emphasizeBest = true;
    }

    public void closeWindow() {
        if (window != null) window.dispose();
    }

    void createPopulation() {
        for (int i = 0; i < nParticles; i++) {
            particle[i] = new HParticle();
            particle[i].init();
        }
    }

    void calculateErrors(int number) {
        for (int i = 0; i < number; i++)
            particle[i].calculateErrors();
    }

    int extractElites(double eliteRatio) {
        int nElites = (int) (eliteRatio * nParticles);
        sortParticles(nElites);
        for (int i = nElites; i < nParticles; i++)
            particle[i] = null;
        return nElites;
    }

    void sortParticles(int nBest) {
        HParticle aux;
        // Bubble-sort particles (only first nBest ones)
        for (int i = 0; i < nBest; i++) {
            for (int j = i + 1; j < nParticles; j++)
                if (particle[i].getPositionError() > particle[j]
                        .getPositionError()) {
                    // NB: particles are sorted according to their ACTUAL error
                    aux = particle[i];
                    particle[i] = particle[j];
                    particle[j] = aux;
                }
        }
    }

    void enhanceElites(int nElites, int maxIterations) {
        for (int i = 0; i < maxIterations && HParticle.getgBestError() > minAcceptableError; i++) {
            calculateErrors(nElites);
            for (int j = 0; j < nElites; j++)
                particle[j].newVelocityAndPosition();
        }
    }

    void ageElites(int nElites) {
        int maxAge = 1, a;
        for (int i = 0; i < nElites; i++) {
            a = particle[i].incrementAge();
            if (a > maxAge) maxAge = a;
        }
        HParticle.setgAge(maxAge);
    }

    void completePopulation(int nElites) {
        for (int i = nElites; i < nParticles; i += 2) {
            HParticle[] twoNewParticles;
            twoNewParticles = selectTwoParticles(nElites);
            twoNewParticles = crossTwoParticles(twoNewParticles);
            twoNewParticles = mutateTwoParticles(twoNewParticles, 0.1);
            twoNewParticles[0].initAfterCrossover();
            twoNewParticles[1].initAfterCrossover();
            if (nParticles - i > 1) {
                particle[i] = twoNewParticles[0];
                particle[i + 1] = twoNewParticles[1];
            } else particle[i] = twoNewParticles[0];
        }
    }

    HParticle[] selectTwoParticles(int nElites) {
        return selectParticles(nElites, 2);
    }

    HParticle[] crossTwoParticles(HParticle[] particles) {
        double aux;
        int startPos, endPos; // positions at which to start and end the
        // crossover
        startPos = myRandom.nextInt(nDimensions);
        while ((endPos = myRandom.nextInt(nDimensions)) == startPos) ;
        if (startPos > endPos) {
            int auxPos = endPos;
            endPos = startPos;
            startPos = auxPos;
        }
        for (int i = startPos; i < endPos; i++) {
            aux = particles[0].getPosition(i);
            particles[0].setPosition(i, particles[1].getPosition(i));
            particles[1].setPosition(i, aux);
        }
        return particles;
    }

    HParticle[] mutateTwoParticles(HParticle[] particles, double probability) {
        particles[0] = mutateParticle(particles[0], probability);
        particles[1] = mutateParticle(particles[1], probability);
        return particles;
    }

    HParticle[] selectParticles(int nElites, int number) {
        HParticle p1, p2;
        HParticle[] newParticles = new HParticle[number];
        for (int i = 0; i < number; i++) {
            p1 = particle[myRandom.nextInt(nElites)];
            p2 = particle[myRandom.nextInt(nElites)];
            if (p1.getPositionError() <= p2.getPositionError()) newParticles[i] = p1.createCopyBasic();
            else newParticles[i] = p2.createCopyBasic();
        }
        return newParticles;
    }

    HParticle mutateParticle(HParticle p, double probability) {
        if (Math.random() < probability) p.mutate(myRandom.nextInt(nDimensions));
        return p;
    }

    public double getBest(int i) {
        return HParticle.getgBest(i);
    }
}
