package game.trainers;

import configuration.CommonUnits;
import configuration.game.trainers.QuasiNewtonConfig;

public class TrainerUnits extends CommonUnits {

    public static final String TRAINER = "trainer";
    private static TrainerUnits instance;

    protected TrainerUnits() {
        //setValue(TRAINER, QuasiNewtonTrainer.class, QuasiNewtonConfig.class);
    }

    public static TrainerUnits getInstance() {
        if (instance == null) {
            instance = new TrainerUnits();
        }
        return instance;
    }

}
