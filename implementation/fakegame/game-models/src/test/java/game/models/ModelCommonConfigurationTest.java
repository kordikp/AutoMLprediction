package game.models;

import game.trainers.TrainerUnits;

import org.junit.Test;

import configuration.models.ModelUnits;

class Testr {
    public static int i = 0;
}

class Test2 extends Testr {

}

public class ModelCommonConfigurationTest {

    @Test
    public void test() {
        System.out.println(ModelUnits.getInstance().getCount());
        System.out.println(TrainerUnits.getInstance().getCount());
        System.out.println(ModelUnits.getInstance().getUnitConfig(1));
    }

}
