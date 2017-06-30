package configuration.classifiers.single;


//import configuration.Slider;
import configuration.classifiers.ClassifierConfigBase;
import game.classifiers.single.KNNClassifier;
import org.ytoh.configurations.annotations.Property;
import org.ytoh.configurations.annotations.SelectionSet;
import org.ytoh.configurations.ui.CheckBox;
import org.ytoh.configurations.ui.SelectionSetModel;
//import utils.UtilsCommon;

public class KNNClassifierConfig extends ClassifierConfigBase {
    @Property(name = "K", description = "K in the K-Nearest neighbours.")
  //  @Slider(value=1,min=1,max=5,multiplicity=1,name="K:")
    protected int nearestNeighbours;

    @Property(name = "Weighted vote", description = "Indicates if the votes should be weighted by similarity.")
//    @CheckBox
    protected boolean weightedVote;

    @Property(name = "Measure type", description = "Measure type for computing distance.")
   // @SelectionSet(key = "measureType", type = SelectionSetModel.class)
    protected SelectionSetModel<String> measureType;

    public KNNClassifierConfig(){
        super();

        String[] measureTypes = new String[]{
            "EuclideanDistance",
            "CamberraDistance",
            "ChebychevDistance",
            "CorrelationSimilarity",
            "CosineSimilarity",
            "DiceSimilarity",
            "DynamicTimeWarpingDistance",
            "InnerProductSimilarity",
            "JaccardSimilarity",
            "ManhattanDistance",
            "MaxProductSimilarity",
            "OverlapSimilarity"
        };
        measureType = new SelectionSetModel<String>(measureTypes);
        measureType.disableAllElements();
        measureType.enableElement(0);

        nearestNeighbours = 1;
        weightedVote = false;
        classRef = KNNClassifier.class;
    }

    public static<T> SelectionSetModel<T> cloneSelectionSet(SelectionSetModel<T> oldSet) {
        SelectionSetModel<T> newSet = new SelectionSetModel<T>(oldSet.getAllElements());
        newSet.disableAllElements();
        newSet.enableElementIndices(oldSet.getEnableElementIndices());
        return newSet;
    }

    public KNNClassifierConfig clone() {
        KNNClassifierConfig newObject = (KNNClassifierConfig)super.clone();

        newObject.setMeasureType(cloneSelectionSet(measureType));
	    return newObject;
    }

    protected String variablesToString() {
        return "(k="+nearestNeighbours+",vote="+weightedVote+",measure="+measureType.getEnabledElements(String.class)[0]+")";
    }

    public SelectionSetModel<String> getMeasureType() { return measureType; }
    public void setMeasureType(SelectionSetModel<String> measureType) { this.measureType = measureType; }

    public boolean getWeightedVote() { return weightedVote; }
    public void setWeightedVote(boolean weightedVote) { this.weightedVote = weightedVote; }

    public int getNearestNeighbours() { return nearestNeighbours; }
    public void setNearestNeighbours(int nearestNeighbours) { this.nearestNeighbours = nearestNeighbours; }

    public double getComplexity(int instances, int dimension, int outputs) {
        double instComplexity = 0.0000051219566024*instances*instances + 0.0001221796547465*instances + 0.5561899698608180;
        double dimComplexity = 0.0758692978631453*dimension + 1.8658843256662500;
        return instComplexity*dimComplexity;
    }

    protected String getComprehensiveClassName() {
        return "KNN";
    }
}
