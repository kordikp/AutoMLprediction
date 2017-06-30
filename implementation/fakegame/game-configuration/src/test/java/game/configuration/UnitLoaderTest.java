package game.configuration;

import org.junit.Test;

import configuration.UnitLoader;

public class UnitLoaderTest {


    @Test
    public void testInstance() {
        UnitLoader ul = UnitLoader.getInstance();
        System.out.println(ul.getList("models.single").size());
        System.out.println(ul.getList("models.single").get(0));

    }

}
