package configuration.classifiers.single;


//import configuration.Slider;
import configuration.classifiers.ClassifierConfigBase;
import game.classifiers.single.DTForestClassifier;
import org.ytoh.configurations.annotations.Property;

public class DTForestClassifierConfig extends ClassifierConfigBase {
    @Property(name = "Trees", description = "Number of trees used in forest.")
  //  @Slider(value=10,min=1,max=30,multiplicity=1,name="Trees:")
    protected int numberOfTrees;

    public DTForestClassifierConfig() {
        super();
        numberOfTrees = 10;
        classRef = DTForestClassifier.class;
    }

    protected String variablesToString() {
        return "(trees="+numberOfTrees+")";
    }

    public int getNumberOfTrees() { return numberOfTrees; }
    public void setNumberOfTrees(int numberOfTrees) { this.numberOfTrees = numberOfTrees; }

    public double getComplexity(int instances, int dimension, int outputs) {
        double instComplexity = 0.0000113465180452*instances*instances + 0.1158053621386770*instances + 3.1099163208715100;
        double dimComplexity = -0.0000929821408066*dimension*dimension + 0.5039273751907100*dimension + 75.8130703991561000;
        return instComplexity*dimComplexity;
    }

    protected String getComprehensiveClassName() {
        return "DTForest";
    }
}
