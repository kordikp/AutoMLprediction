package game.classifiers;

import configuration.CfgTemplate;
import game.classifiers.single.ClassifierModel;
import game.data.GameData;
import game.data.GlobalData;
import game.data.MinMaxDataNormalizer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import configuration.classifiers.ClassifierConfig;


/**
 * A singleton - classifiers container - all classifiers held in the memory will be referenced from here
 */
@Deprecated
public class Classifiers implements Serializable {
    private Vector classifiers = new Vector();

    private Classifiers() {
    }

    private static Classifiers uniqueInstance;

    public static synchronized Classifiers getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Classifiers();
        }
        return uniqueInstance;
    }

    /**
     * returns classifier at position index
     *
     * @param index position of the classifier
     * @return Classifier
     */
    public ConnectableClassifier getClassifier(int index) {
        if (index >= classifiers.size()) return null;
        return (ConnectableClassifier) classifiers.elementAt(index);
    }

    /**
     * Stores classifier at the position index
     *
     * @param m     Classifier to be stored
     * @param index position to store the classifier
     */
    public void storeClassifierAt(Classifier m, int index) {
        if (index <= classifiers.size()) classifiers.addElement(m);
        else classifiers.setElementAt(m, index);
    }

    /**
     * Removes all classifiers
     */
    public void deleteAllClassifiers() {
        classifiers.removeAllElements();
    }

    /**
     * Appends Classifier to the end of the field classifiers
     *
     * @param m Classifier to be appended
     */
    public void appendClassifier(Classifier m) {
        classifiers.addElement(m);
    }

    /**
     * Deletes modet at position index
     *
     * @param index which classifier should be deleted
     */
    public void deleteClassifierAt(int index) {
        if (index < classifiers.size()) {
            classifiers.removeElementAt(index);
        }
    }

    /**
     * Returns true when at least one classifier is present in the memory
     *
     * @return false if vector of classifiers is empty
     */
    public boolean classifierPresent() {
        return classifiers.size() > 0;
    }

    /**
     * Returns classifiers in the array
     *
     * @return array of classifiers
     */
    public Classifier[] getClassifiersArray() {
        // if (classifiers.size() == 0) return null;
        Classifier[] ar = new Classifier[classifiers.size()];
        for (int i = 0; i < classifiers.size(); i++) ar[i] = getClassifier(i);
        return ar;
        //return (Classifier[])classifiers.toArray();
    }


    public int getClassifiersNumber() {
        return classifiers.size();
    }

    /**
     * Function to get classifier or ensemble by the class name
     *
     * @param classifierClassName Name of the single classifier (single.LinearClassifier) or ensemble (ensemble.ModelBagging) etc.
     * @return new instance of Classifier
     */
    public static ClassifierModel newInstancebyClassName(Class classifierClassName) {
        try {
            return (ClassifierModel) classifierClassName.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createNewClassifier(CfgTemplate what, GameData data) {

        ClassifierConfig cfg = (ClassifierConfig) what;
        int num = data.getInstanceNumber();
        cfg.setMaxLearningVectors(num);
        MinMaxDataNormalizer norm = new MinMaxDataNormalizer();
        norm.init(data.getInputVectors(), data.getOutputAttrs());  // use minmax normalization within the connectable model

        ConnectableClassifier classifier = new ConnectableClassifier();
        classifier.init(what, data.getInputFeatures(), norm);

        for (int j = 0; j < num; j++) {
            data.publishVector(j);
            classifier.storeLearningVector(data.getTargetOutputs());
        }
        classifier.learn();
        if (!classifier.isLearned()) return false;
        appendClassifier(classifier);
        //System.out.println(classifier.toEquation());
        return true;
    }

    public static ArrayList<Classifier> loadClassifiersfromXMLStream(InputStream is) {
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(Classifiers.class.getClassLoader());
        ArrayList<Classifier> m = (ArrayList<Classifier>) xstream.fromXML(is);
        return m;
    }

    public static void saveClassifiersToXMLStream(OutputStream os, ArrayList<Classifier> m) {
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(Classifiers.class.getClassLoader());
        xstream.toXML(m, os);
    }
}