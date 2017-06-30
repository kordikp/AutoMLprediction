package configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cepekm1
 * Date: Oct 20, 2010
 * Time: 3:09:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnitHolder<T> {

    private ArrayList<T> units;
    private ArrayList<String> unitNames;
    private ClassLoader cl;

    protected UnitHolder(String unitID) {

        List<String> unitList = UnitLoader.getInstance().getClassNames(unitID);
        Iterator<String> unitIterator = unitList.iterator();

        units = new ArrayList<T>(unitList.size());
        unitNames = new ArrayList<String>(unitList.size());

        while (unitIterator.hasNext()) {
            units.add(null);
            unitNames.add(unitIterator.next());
        }

        cl = getClass().getClassLoader();
    }

    public T getUnit(int i) {

        try {
            if (units.get(i) == null) {
                System.out.printf("Trying to load %s ... ", unitNames.get(i));
                Class c = cl.loadClass(unitNames.get(i));
                T unit = (T) c.getConstructor(boolean.class).newInstance(false);
                if (unit == null) {
                    throw new NullPointerException();
                }
                units.set(i, unit);
                //units.set(i, (T) c.newInstance());
                System.out.printf("OK\n");
            }
        } catch (ClassNotFoundException e) {
            System.out.printf("fail\n");
            System.out.printf("Definition of class " + unitNames.get(i) + " was not found. Do something.\n");
            System.out.printf("Error message - %s\n", e.getMessage());
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(-1);

        } catch (InstantiationException e) {
            System.out.printf("fail\n");
            System.out.printf("Creation of new instance of class " + unitNames.get(i) + " has failed. Do something.\n");
            System.out.printf("Error message - %s\n", e.getMessage());
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(-1);

        } catch (IllegalAccessException e) {
            System.out.printf("fail\n");
            System.out.printf("Creation of new instance of class " + unitNames.get(i) + " has failed. Do something.\n");
            System.out.printf("Error message - %s\n", e.getMessage());
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(-1);
        } catch (NoSuchMethodException e) {
            System.out.printf("fail\n");
            System.out.printf("Creation of new instance of class " + unitNames.get(i) + " has failed. Do something.\n");
            System.out.printf("Error message - %s\n", e.getMessage());
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(-1);
        } catch (InvocationTargetException e) {
            System.out.printf("fail\n");
            System.out.printf("Creation of new instance of class " + unitNames.get(i) + " has failed. Do something.\n");
            System.out.printf("Error message - %s\n", e.getMessage());
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(-1);
        }

        return units.get(i);
    }

    public int getSize() {
        return unitNames.size();
    }

    public List<T> getAllUnits() {
        return units.subList(0, units.size());
    }

    public String getClassName(int i) {
        return unitNames.get(i);
    }

}
