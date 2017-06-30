package game.trainers;

import game.trainers.gradient.NumericalDiffWrapper;
import game.trainers.gradient.Marquardt.MarquardtMinimiserException;
import configuration.game.trainers.LevenbergMarquardtConfig;


public class LevenbergMarquardtTrainer extends Trainer {

    private LevenbergMarquardtMinimiserImpl minim;

    private double gradStepMult;
    private double hessStepMult;

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        LevenbergMarquardtConfig cf = (LevenbergMarquardtConfig) cfg;
        gradStepMult = cf.getGradStepMult();
        hessStepMult = cf.getHessStepMult();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);

        NumericalDiffWrapper wrap = new NumericalDiffWrapper(this, gradStepMult, hessStepMult);
        wrap.init();
        minim = new LevenbergMarquardtMinimiserImpl(this, wrap, coef);
    }

    public void teach() {

        double p[] = new double[minim.getDim()];

        try {
            minim.setInitParams(p);
            minim.minimise();
        } catch (MarquardtMinimiserException e) {
            // DROPPING exception, singular matrix warnings
            System.out.println(e.getMessage());
        }
//        System.out.println("FINISHED");
    }

    public String getMethodName() {
        return "Levenberg-Marquardt";
    }


    public Class getConfigClass() {
        return LevenbergMarquardtConfig.class;
    }

    public boolean allowedByDefault() {
        return false;
    }
}