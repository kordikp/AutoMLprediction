package game.trainers;

import configuration.game.trainers.PowellConfig;

public class PowellTrainer extends Trainer {

    private PowellMinimiserImpl minim;
    double tolerance;
    private int interMax;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        PowellConfig cf = (PowellConfig) cfg;
        tolerance = cf.getTolerance();
        interMax = cf.getIterMax();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);
        minim = new PowellMinimiserImpl(this, coef);
        minim.setIterMax(interMax);
    }

    public void teach() {

        double p[] = new double[minim.getDim() + 1];
        for (int i = 1; i <= minim.getDim(); i++) p[i] = this.getBest(i - 1);
        p[0] = 0;

        try {
            minim.minimise(p, tolerance);
        } catch (Exception ignored) {
        }
    }

    public String getMethodName() {
        return "Powell";
    }

    public Class getConfigClass() {
        return PowellConfig.class;
    }

    public boolean allowedByDefault() {
        return false;
    }
}
