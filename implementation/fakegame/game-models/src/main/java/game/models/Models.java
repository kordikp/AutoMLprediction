package game.models;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import configuration.CfgTemplate;
import configuration.models.ModelConfig;
import configuration.models.TrainerSelectable;
import game.data.GlobalData;
import game.data.MinMaxDataNormalizer;
import game.data.OutputAttribute;
import game.data.OutputProducer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

/**
 * A singleton - models container - all models held in the memory will be referenced from here
 */
@Deprecated
public class Models implements Serializable {
    private Vector models = new Vector();

    private Models() {
    }

    private static Models uniqueInstance;

    public static synchronized Models getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Models();
        }
        return uniqueInstance;
    }

    /**
     * returns model at position index
     *
     * @param index position of the model
     * @return Model
     */
    public Model getModel(int index) {
        if (index >= models.size()) return null;
        return (Model) models.elementAt(index);
    }

    /**
     * Stores model at the position index
     *
     * @param m     Model to be stored
     * @param index position to store the model
     */
    public void storeModelAt(Model m, int index) {
        if (index <= models.size()) models.addElement(m);
        else models.setElementAt(m, index);
    }

    /**
     * Removes all models
     */
    public void deleteAllModels() {
        models.removeAllElements();
    }

    /**
     * Appends Model to the end of the field models
     *
     * @param m Model to be appended
     */
    public void appendModel(Model m) {
        models.addElement(m);
    }

    /**
     * Deletes modet at position index
     *
     * @param index which model should be deleted
     */
    public void deleteModelAt(int index) {
        if (index < models.size()) {
            models.removeElementAt(index);
        }
    }

    /**
     * returns list of models of particular target variable
     *
     * @param target_variable The index of output variable that is modelled
     * @return list of models
     */
    public Model[] getModelsOfVariable(int target_variable) {
        return null; //todo
    }

    /**
     * Returns true when at least one model is present in the memory
     *
     * @return false if vector of models is empty
     */
    public boolean modelPresent() {
        return models.size() > 0;
    }

    /**
     * Returns models in the array
     *
     * @return array of models
     */
    public Model[] getModelsArray() {
        //if(models.size()==0)return null;
        Model[] ar = new Model[models.size()];
        for (int i = 0; i < models.size(); i++) ar[i] = getModel(i);
        return ar;
        //return (Model[])models.toArray();
    }

    /**
     * returns the unique name of the model (radiation 2)
     *
     * @param n
     */

    public String unique(int n) {
        String name = getModel(n).getName();
        int k = 0;
        for (int i = 0; i < n; i++) {
            if ((getModel(i) != null) && (getModel(i).getName() != null) && (name != null)) {
                if (getModel(i).getName().compareTo(name) == 0) {
                    k++;
                }
            }
        }

        //if (k > 0) {
        name += " " + Integer.toString(k);

        //}
        return name;
    }


    /**
     * looks for the model
     *
     * @param n
     */

    public int findNet(Model n) {
        int num = -1;
        for (int i = 0; i < models.size(); i++) {
            if (getModel(i) == n) {
                return i;
            }
        }
        return num;
    }


    /**
     * looks for the model by name
     *
     * @param name
     */

    public int findNet(String name) {
        if (name.indexOf(" ") > 0) {
            int k = Integer.valueOf(name.substring(name.indexOf(" ") + 1));
            for (int i = 0; i < models.size(); i++) {
                if (getModel(i) != null) {
                    if (getModel(i).getName().compareTo(name.substring(0, name.indexOf(" "))) == 0) {
                        if (k-- == 0) {
                            return i;
                        }
                    }
                }
            }
        }
        /*
         * //todo FIXME inkonzistentne spravanie
         * Ak mame iba jeden model, tak ho necislujeme a nasledujuci kod je v poriadku
         * Ak mame viacero modelov (ensemble) tak nasledujuci for-cyklus vrati prvu najdenu siet
         * s danym menom, t.j. nakde siet s 'menom' "NAME 0"
         */
        for (int i = 0; i < models.size(); i++) {
            if (getModel(i) != null) {
                if (getModel(i).getName().compareTo(name) == 0) { //the same name
                    return i;
                }
            }
        }
        return -1; //search failed
    }


    /**
     * looks for the group of the models modelling the same output
     */

    public int findGnetGroup() {
        int cr, num = 0;
        //TODO
        //  if (Controls.getInstance().getGnet() == null) {
        //     return 0;
        // }
        // cr = Controls.getInstance().getGnet().getTargetVariable();
        //for (int i = 0; i < models.size(); i++) {
        //  if (getModel(i) != null) {
        //       if (getModel(i).getTargetVariable() == cr) {
        //          num++;
        //     }
        //  }
        //}
        return num;
    }


    /**
     * returns the group of the models modelling the same output
     */

    public Model[] getGnetGroup() {
        int cr, num = 0;
        Model[] group = new Model[models.size()];
        //  if (Controls.getInstance().getGnet() == null) {
        return null;
        //  }
        // cr = Controls.getInstance().getGnet().getTargetVariable();
//        for (int i = 0; i < models.size(); i++) {
//            if (getModel(i) != null) {
//                if (getModel(i).getTargetVariable() == cr) {
//                    group[num++] = getModel(i);
//                }
//            }
//        }
        //return group;
    }

    public int getModelsNumber() {
        return models.size();
    }

    /**
     * Returns true when model is of game.neurons.GAMEnetwork class
     *
     * @param m Model
     * @return true when model class if game.neurons.GAMEnetwork
     */
    public static boolean isGAMEClass(Model m) {
        return m.getClass().getName().compareTo("game.neurons.GAMEnetwork") == 0;
    }

    /**
     * @param cols the order of model
     * @return name of the model
     */

    public String getModelName(int cols) {
        int i = 0;
        if (cols < GlobalData.getInstance().getONumber()) {
            return ((OutputAttribute) GlobalData.getInstance().oAttr.get(cols)).getName(); //.substring(1,3);
        }
        int cnt = 0;
        do {

            if (cnt == cols - GlobalData.getInstance().getONumber()) {
                return "M" + (getModel(i).getName().length() < 5 ? getModel(i).getName() : getModel(i).getName().substring(1, 4)) + Integer.toString(cols - GlobalData.getInstance().getONumber());
            }
            cnt++;
            i++;
        } while (i < models.size());
        return "ERROR!";
    }

    public void generateSingleModel(ModelLearnable m, TrainerSelectable cfg) {

        createNewModel(cfg);

    }


    /**
     * Function to get model or ensemble by the class name
     *
     * @param modelClassName Name of the single model (single.LinearModel) or ensemble (ensemble.ModelBagging) etc.
     * @return new instance of Model
     */
    public static ModelLearnable newInstancebyClassName(Class modelClassName) {
        try {
            return (ModelLearnable) modelClassName.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createNewModel(CfgTemplate what) {
        GlobalData data = GlobalData.getInstance();
        data.refreshDataVectors();
        data.refreshInputFeatures();
        ModelConfig cfg = (ModelConfig) what;
        int num = data.getInstNumber();
        //cfg.setMaxInputsNumber(GlobalData.getInstance().getINumber()-2);
        cfg.setMaxLearningVectors(num);
        //ModelLearnable model = newInstancebyClassName(what.getClassRef());
        ConnectableModel model = new ConnectableModel();
        Vector<OutputProducer> inp = data.getInputFeatures();
        MinMaxDataNormalizer norm = new MinMaxDataNormalizer();
        norm.init(data.getIvectors(), data.getOattrs());  // use minmax normalization within the connectable model
        model.init(what, inp, norm);
        ModelLearnable mo = (ModelLearnable) model.getModel();
        model.setName(((OutputAttribute) data.oAttr.get(cfg.getTargetVariable())).getName());
        for (int j = 0; j < num; j++) {
            data.publishVector(j);
            model.storeLearningVector(data.getTargetOutput(cfg.getTargetVariable()));
        }
        mo.learn();
        appendModel(model);
        System.out.println(model.toEquation());
        return true;
    }

    public static ArrayList<Model> loadModelsfromXMLStream(InputStream is) {
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(Models.class.getClassLoader());
        ArrayList<Model> m = (ArrayList<Model>) xstream.fromXML(is);
        return m;
    }

    public static void saveModelsToXMLStream(OutputStream os, ArrayList<Model> m) {
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(Models.class.getClassLoader());
        xstream.toXML(m, os);
    }
}
