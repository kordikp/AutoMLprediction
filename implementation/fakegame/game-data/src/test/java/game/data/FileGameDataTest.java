package game.data;

import org.junit.Test;

public class FileGameDataTest {

    public static final String DATA_FILE = "/home/vlada/workspace/fakegame3/game-data/src/main/resources/data/iris.txt";
    private static FileGameData gameData = null;

    @Test
    public void readFile() {
        gameData = new FileGameData(DATA_FILE);
    }

}
