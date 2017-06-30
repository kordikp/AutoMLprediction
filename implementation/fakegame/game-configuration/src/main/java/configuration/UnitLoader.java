/**
 * @author Pavel Kordik
 * @version 0.90
 */
package configuration;

//import game.configuration.ClassWithConfigBean;

import game.configuration.Configurable;
import game.utils.FilePathLocator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * serves as unit classes loader
 */


public class UnitLoader implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(UnitLoader.class);
    /**
     * names of the classes are stored in file "units.cfg"
     */
    private final static String CLASS_NAME_FILE = "units.cfg";
    /**
     * string "rem" in the beginning of the line means not to load the class
     */
    private final static String DISABLED = "rem";


    private Map<String, Map<String, String>> items = new HashMap<String, Map<String, String>>();


    public static final String preprocessingTag = "preprocessing";
    public static final String visualisationTag = "visualization";
    public static final String trainersTag = "trainers";
    public static final String modelsTag = "models.single";
    public static final String classifiersTag = "classifiers.single";
    public static final String ensembleModelsTag = "models.ensemble";
    public static final String ensembleClassifiersTag = "classifiers.ensemble";
    public static final String neuronsTag = "neurons";
    public static final String statisticsTag = "statistic";


    private UnitLoader() {
        refreshClasses();
    }


    private static UnitLoader uniqueInstance;

    public static synchronized UnitLoader getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new UnitLoader();
        }
        return uniqueInstance;
    }

    public static void replaceInstance(UnitLoader u) {
        uniqueInstance = u;
    }
    /*

      to reallocate classes

      transient public Vector units;

      transient public Vector trainingMethods;

      transient public Vector graphClasses;

      after deserialization

    */

    public void refreshClasses() {
        readSourceFile();
    }

    /**
     * all classes specified in the file "units.cfg" are loaded
     */

    void readSourceFile() {
        String line;

//        String cp = System.getProperty("java.class.path");
//        System.out.println(cp);

        String unitspath = FilePathLocator.getInstance().findFile(CLASS_NAME_FILE);
        //URL unitspath = this.getClass().getResource("cfg/"+CLASS_NAME_FILE);
        if (unitspath == null) {
            System.out.printf("Sorry file with units (units.cfg) was not found :(. Can not continue.\n");
            System.out.flush();
            System.exit(-1);
        }

        System.out.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        System.out.printf("Using units.cfg from following path: %s\n", unitspath);
        System.out.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");

        try {
            FileReader fr = new FileReader(unitspath);
            BufferedReader sourceFile = new BufferedReader(fr);
            String className;
            String description;
            while ((line = sourceFile.readLine()) != null) {
                if (!line.startsWith(DISABLED)) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    if (!st.hasMoreTokens()) continue; // class name missing
                    className = st.nextToken();
                    if (st.hasMoreTokens()) description = st.nextToken();
                    else description = null;
                    // try {
                    // Kvuli spravnemu nacitani FG jako pluginu pro Pentaho
                    // pouzivam CLassLoader, ktery nacetl tuto tridu a ne
                    // SystemClassLoader... Ivo Lasek
//                        Class c = ClassLoader.getSystemClassLoader().loadClass(className);
                    //  Class c = getClass().getClassLoader().loadClass(className);
                    //  Class cfgClass = ((Configurable) c.newInstance()).getConfigClass();
                    //  ClassWithConfigBean cwc;
                    //  if (cfgClass != null) {
                    //      Object cfg = (Object) cfgClass.newInstance();
                    //     cwc = new ClassWithConfigBean(description, c, cfg);
                    //  } else cwc = new ClassWithConfigBean(description, c, null);


                    if (className.contains("preprocessing.methods")) {
                        setValue(preprocessingTag, className, description);
                    }
                    if (className.contains("visualization")) {
                        setValue(visualisationTag, className, description);
                    }
                    if (className.contains("trainers")) {
                        setValue(trainersTag, className, description);
                    }
                    if (className.contains("models.single")) {
                        setValue(modelsTag, className, description);
                    }
                    if (className.contains("classifiers.single")) {
                        setValue(classifiersTag, className, description);
                    }
                    if (className.contains("models.ensemble")) {
                        setValue(ensembleModelsTag, className, description);
                    }
                    if (className.contains("classifiers.ensemble")) {
                        setValue(ensembleClassifiersTag, className, description);
                    }
                    if (className.contains("neurons")) {
                        setValue(neuronsTag, className, description);
                    }
                    if (className.contains("statistic")) {
                        setValue(statisticsTag, className, description);
                    }

                    //  } catch (ClassNotFoundException e) {
                    //      e.printStackTrace();
                    //  } catch (IllegalAccessException e) {
                    //     e.printStackTrace();
                    // } catch (InstantiationException e) {
                    //    e.printStackTrace();
                    // }

                    //
                }
            }
            fr.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
//            System.out.println("Error occured while reading " + CLASS_NAME_FILE + " (probably file not found - now looking in directory " + System.getProperty("user.dir") + ")");
            // System.out.println("Error occured while reading ... \n class names file " + CLASS_NAME_FILE);
            System.out.println("File " + unitspath + " not found, check the GAME installation.");
        } catch (java.lang.NullPointerException npe) {
            npe.printStackTrace();
        } catch (java.lang.NumberFormatException ioe) {
            ioe.printStackTrace();
        }

    }

    protected void setValue(String unitTypeTag, String className, String description) {
        Map<String, String> map = items.get(unitTypeTag);
        if (map == null) {
            map = new HashMap<String, String>();
            items.put(unitTypeTag, map);
        }
        if (description == null) {
            description = className;
        }
        map.put(description, className);
    }

    public List<String> getList(String type) {
        return new LinkedList<String>(items.get(type).keySet());
    }

    public List<String> getClassNames(String typeTag) {

        return new LinkedList<String>(items.get(typeTag).values());

    }

    public CfgTemplate getConfiguration(String type, String value) {
        Map<String, String> map = items.get(type);
        String className = map.get(value);

        //ClassWithConfigBean cwc = null;
        Class c;
        CfgTemplate cfg = null;
        try {
            c = getClass().getClassLoader().loadClass(className);

            Class cfgClass = ((Configurable) c.newInstance()).getConfigClass();

            cfg = (CfgTemplate) cfgClass.newInstance();
            cfg.setClassRef(c);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cfg;


    }

}

