package configuration;

import game.configuration.ClassWithConfigBean;
import game.configuration.Configurable;

import java.io.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Factory for creating configurations.
 *
 * @author Vladimir Mezera
 */
public class ConfigurationFactory {


    protected ConfigurationFactory() {

    }

    public static ClassWithConfigBean getClassWithConfigBean(String description, Class<? extends Configurable> aClass, Object aConfig) {
        return new ClassWithConfigBean(description, aClass, aConfig);
    }

    public static CfgTemplate getConfiguration(String file) {
        //TODO ve vsech faktory sjednoti tohle nacitani.
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(CfgTemplate.class.getClassLoader());
        CfgTemplate m = null;
        try {
            File mfile = new File(file);
            FileReader fr = new FileReader(mfile.getAbsolutePath());
            m = (CfgTemplate) xstream.fromXML(fr);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    public static CfgTemplate readConfiguration(StringReader sr) {
        //TODO ve vsech faktory sjednoti tohle nacitani.
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(CfgTemplate.class.getClassLoader());
        CfgTemplate m = null;
        try {
            m = (CfgTemplate) xstream.fromXML(sr);
            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    public static Object getConfigurationObject(String file) {
        XStream xstream = new XStream(new DomDriver());
        xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
        Object m = null;
        try {
            File mfile = new File(file);
            FileReader fr = new FileReader(mfile.getAbsolutePath());
            m = xstream.fromXML(fr);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    public static ClassWithConfigBean getConfiguration(Class<CfgTemplate> aClass) {
        try {
            return new ClassWithConfigBean(aClass, aClass.newInstance());
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    public static void saveConfiguration(Object module, String filename) {
        XStream xstream = new XStream();
        xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
        try {

            FileWriter fw = new FileWriter(new File(filename));
            xstream.toXML(module, fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
