/**
 * @author Oleg_Kovarik@post.cz
 * @version 1.2
 * <p>
 * <p>Title: API - HuntingSite</p>
 * <p>Description: class for API hunting site</p>
 */

package game.trainers.ant.api;

// import game.trainers.gradient.Newton.Uncmin_methods;

class HuntingSite {
    private int dimensions;        // vector of variables size
    private int starvation;        // number of iterations before starvation
    // main
    public double position[];
    private int lastSuccess;
    public double fitness;


    public HuntingSite(double[] nestPosition, double radius, int dimensions, int starvation) {
        this.dimensions = dimensions;
        this.starvation = starvation;

        position = new double[dimensions];
        lastSuccess = 1;
        fitness = Double.MAX_VALUE;

        // generate random position near HS
        if (dimensions > 1) {
            // coount shift vector using n-dimensional spherical coordinates
            double angles[] = new double[dimensions - 1];
            double distance = Math.random() * radius;

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
                position[i] += nestPosition[i];

        } else {
            // for 1D
            position[0] = nestPosition[0] + (Math.random() * radius * 2) - radius;
        }
    }

    public void success(double[] pos, double fit) {
        fitness = fit;
        lastSuccess = 0;
        System.arraycopy(pos, 0, position, 0, dimensions);
    }

    public void notsuccess() {
        lastSuccess++;
    }

    public double getPosition(int i) {
        return position[i];
    }

    public boolean starving() {
        return (lastSuccess > starvation);
    }
}
