package game.trainers;


import configuration.game.trainers.SADEConfig;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SADETrainer extends Trainer implements game.trainers.gartou.ObjectiveFunction {
    private transient game.trainers.gartou.Gartou GA;
    int cnt;
    int rec, draw;
    boolean returnToDomain;
    double domainSize;
    private int fitnessCallsLimit;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        SADEConfig cf = (SADEConfig) cfg;
        rec = cf.getRec();
        draw = cf.getDraw();
        returnToDomain = cf.isReturnToDomain();
        domainSize = cf.getDomainSize();
        fitnessCallsLimit = cf.getFitnessCallsLimit();

        game.trainers.gartou.General.initializeRandoms();
        GA = new game.trainers.gartou.Gartou(this);
        GA.fitnessCallsLimit = fitnessCallsLimit;
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        GA.run();
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "SADE - genetics method";
    }


    /**
     * no config class
     */
    public Class getConfigClass() {
        return SADEConfig.class;
    }

    public int getDim() {
        return coefficients;
    }

    public double getDomain(int x, int y) {
        if (y == 0) {
            return -domainSize;
        }
        if (y == 1) {
            return domainSize;
        }
        return 0;
    }

    public double getOptimum() {
        return 0.0;
    }

    public double getPrecision() {
        return 0.0001;
    }

    public boolean getReturnToDomain() {
        return returnToDomain;
    }

    public double value(double[] x) {
        double error = getAndRecordError(x, rec, draw, true);
        return -error;
    }

    public void evaluate(double[] x) {
    }

    public boolean allowedByDefault() {
        return false;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return true;
    }
}
