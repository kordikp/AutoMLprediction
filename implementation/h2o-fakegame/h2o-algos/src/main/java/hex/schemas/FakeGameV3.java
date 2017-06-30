package hex.schemas;

import hex.fakegame.FakeGame;
import hex.fakegame.FakeGameModel;
import water.api.API;
import water.api.schemas3.ModelParametersSchemaV3;

import java.io.File;
import java.nio.file.Path;

public class FakeGameV3 extends ModelBuilderSchema<FakeGame, FakeGameV3, FakeGameV3.FakeGameParametersV3> {

    public static final class FakeGameParametersV3 extends ModelParametersSchemaV3<FakeGameModel.FakeGameParameters, FakeGameParametersV3> {
        static public String[] fields = new String[]{
                "training_frame",
                "validation_frame",
                "response_column",
                "model_config",
                "ignored_columns"
        };

        @API(help = "Model configuration")
        public String model_config;

    } // FakeGameParametersV2

}
