package hex.schemas;

import hex.fakegame.FakeGameModel;
import water.api.API;
import water.api.schemas3.ModelOutputSchemaV3;
import water.api.schemas3.ModelSchemaV3;

public class FakeGameModelV3 extends ModelSchemaV3<FakeGameModel, FakeGameModelV3, FakeGameModel.FakeGameParameters, FakeGameV3.FakeGameParametersV3, FakeGameModel.FakeGameOutput, FakeGameModelV3.FakeGameModelOutputV3> {

  public static final class FakeGameModelOutputV3 extends ModelOutputSchemaV3<FakeGameModel.FakeGameOutput, FakeGameModelOutputV3> {
    // Output fields

  }


  public FakeGameV3.FakeGameParametersV3 createParametersSchema() { return new FakeGameV3.FakeGameParametersV3(); }
  public FakeGameModelOutputV3 createOutputSchema() { return new FakeGameModelOutputV3(); }
}
