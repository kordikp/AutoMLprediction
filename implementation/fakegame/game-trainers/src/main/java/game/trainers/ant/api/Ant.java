/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: API - ant</p>
 * <p>Description: class for API ant</p>
 */

package game.trainers.ant.api;

import game.trainers.gradient.Newton.Uncmin_methods;

import java.util.Random;
import java.util.Vector;

class Ant {
    // static

    private int dimensions;        // vector of variables size
    // static public int antsCount;
    private Uncmin_methods trainer;

    private Random ran;

    // main
    private Nest nest;

    private int number;        // ant number
    private double radius;    // search radius
    private double localRadius;
    private int huntingSitesCount;    // number of hunting sites for one ant

    private Vector huntingSites;


    private boolean huntingSiteAdded;    // new hunting place added in last iteration?
    private int lastCreatedHS;            // number of last created hunting site
    private int lastSuccessful;            // last successful hunting site
    private int lastSuccess;            // last success n iterations
    private int lastVisitedHS;
    private int starvation;

    private double position[];
    private double fitness;

    // static DecimalFormat df;

    public Ant(int number, int dimensions, int huntingSitesCount, int antsCount, Nest nest, Uncmin_methods train, int starvation) {
        this.number = number;
        this.dimensions = dimensions;
        this.huntingSitesCount = huntingSitesCount;
        this.nest = nest;
        this.trainer = train;
        this.starvation = starvation;

        radius = 0.1 * Math.pow(Math.pow(100.0, 1.0 / antsCount), (number + 1));
        localRadius = radius / 10.0;
        fitness = Double.MAX_VALUE;
        lastSuccess = 1;

        position = new double[dimensions];
        huntingSites = new Vector();
        ran = new Random();

        // df = new DecimalFormat();
        // df.setMaximumFractionDigits(4);
        // df.setMinimumFractionDigits(4);
    }

/*	public static void firstInit(Uncmin_methods train, int dimensions, int antsCount, int starvation) {
        HuntingSite.firstInit(dimensions, starvation);
	}*/

    public boolean huntingSiteAdded() {
        return huntingSiteAdded;
    }

    public void checkHuntingSitesQueue() {
        // if last search successful do nothing
        if (lastSuccess > 0) {
            // add site ?
            if (huntingSites.size() < huntingSitesCount) {
                huntingSites.add(new HuntingSite(nest.position, radius, dimensions, starvation));
                huntingSiteAdded = true;
            } else huntingSiteAdded = false;

        }
    }

    public int lastCreatedHS() {
        return lastCreatedHS;
    }

    public boolean lastSearchSuccessful() {
        return (lastSuccess == 0);
    }

    public int lastVisitedHS() {
        return lastVisitedHS;
    }

    public int getRandomHS() {
        return ran.nextInt(huntingSites.size());
    }

    public void forgetAll() {
        fitness = Double.MAX_VALUE;
        huntingSites.clear();
        lastSuccess = 1;
    }

    public void explore(int HS) {
        lastVisitedHS = HS;

        // generate random position near HS
        if (dimensions > 1) {
            // coount shift vector using n-dimensional spherical coordinates
            double angles[] = new double[dimensions - 1];
            double distance = Math.random() * localRadius;

            // angles
            for (int i = 0; i < dimensions - 2; i++) angles[i] = Math.random() * Math.PI;
            angles[dimensions - 2] = Math.random() * (Math.PI * 2);

            // vector <0, dimensions-2>
            for (int i = 0; i < dimensions - 1; i++) {
                position[i] = distance;
                for (int j = 0; j < i; j++)
                    position[i] *= Math.sin(angles[j]);
                position[i] *= Math.cos(angles[i]);
            }

            // position [dimensions-1]
            position[dimensions - 1] = distance;
            for (int j = 0; j < dimensions - 1; j++)
                position[dimensions - 1] *= Math.sin(angles[j]);

            // add to hunting site position
            for (int i = 0; i < dimensions; i++)
                position[i] += ((HuntingSite) huntingSites.elementAt(HS)).getPosition(i);

        } else {
            // for 1D
            position[0] = ((HuntingSite) huntingSites.elementAt(HS)).getPosition(0) + (Math.random() * radius * 2) - radius;
        }

        double fit = trainer.f_to_minimize(position);

        if (fit < fitness) {
            lastSuccessful = HS;
            lastSuccess = 0;
            ((HuntingSite) huntingSites.elementAt(HS)).success(position, fit);
            fitness = fit;
        } else {
            lastSuccess++;
            ((HuntingSite) huntingSites.elementAt(HS)).notsuccess();
            if (((HuntingSite) huntingSites.elementAt(HS)).starving()) huntingSites.remove(HS);
        }

        nest.compareGlobal(fitness, position);

    }

    public double getFitness() {
        return fitness;
    }

    public void tandemRun(Ant ant) {
        /*
		 * find best HS for both ant and replace
		 */
        double f1, f2;
        int pos1, pos2;
        pos1 = 0;
        pos2 = 0;
        f1 = Double.MAX_VALUE;
        f2 = Double.MAX_VALUE;

        for (int i = 0; i < ant.huntingSites.size(); i++) {
            if (((HuntingSite) ant.huntingSites.elementAt(i)).fitness < f2) {
                pos2 = i;
                f2 = ((HuntingSite) ant.huntingSites.elementAt(i)).fitness;
            }
        }

        for (int i = 0; i < huntingSites.size(); i++) {
            if (((HuntingSite) huntingSites.elementAt(i)).fitness < f1) {
                pos1 = i;
                f1 = ((HuntingSite) huntingSites.elementAt(i)).fitness;
            }
        }

        HuntingSite HS1 = ((HuntingSite) huntingSites.elementAt(pos1));
        HuntingSite HS2 = ((HuntingSite) ant.huntingSites.elementAt(pos2));

        System.arraycopy(HS1.position, 0, HS2.position, 0, dimensions);
        HS2.fitness = HS1.fitness;

    }
	
    /*
        public void println() {
		System.out.println("Ant "+number+": ");

    	for (int j=0; j<huntingSites.size(); j++) {
    		System.out.print("HS "+j+": ");
        	for (int k=0; k<dimensions; k++) {
        		System.out.print( df.format( ((HuntingSite) huntingSites.get(j)).getPosition(k) ) + " ");
        	}
    		System.out.println();
    	}
	}
    */
}
