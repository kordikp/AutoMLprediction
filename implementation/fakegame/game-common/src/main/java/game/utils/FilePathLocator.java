package game.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: lagon
 * Date: Jun 30, 2008
 * Time: 5:27:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilePathLocator {

    private static FilePathLocator locatorPtr = null;
    private String rootDirectory;
    private String fileSep;

    private FilePathLocator() {
        rootDirectory = findRootDirectory();
        fileSep = System.getProperty("file.separator");
    }

    private String findRootDirectory() {
        System.out.println("Debug: File PathSepartor" + File.separator);
        URL myURL = this.getClass().getResource("FilePathLocator.class");

        String s = myURL.getFile();
        System.out.printf("%s\n", s);
        int index = s.indexOf(".jar!/game");

        if (index >= 0) {
            //Jsme v JARu
            s = s.substring(0, index);
            index = s.lastIndexOf("/");
            if (index < 0) {
                index = s.lastIndexOf("\\");

                if (index < 0) {
                    //TODO
                    //DebugInfo.putErrorMessage("Error in path. No (back)slash found. Could not determine running path.");
                    System.exit(-1);
                }
            }

            s = s.substring(0, index) + File.separator + "..";
        } else {
            //Rozbaleno
            index = s.lastIndexOf("/");
            if (index < 0) {
                index = s.lastIndexOf("\\");

                if (index < 0) {
                    //TODO
                    // DebugInfo.putErrorMessage("Error in path. No (back)slash found. Could not determine running path.");
                    System.exit(-1);
                }
            }

            s = s.substring(0, index) + File.separator + "..";
        }

        URI uri = null;
        // try {
        System.out.println("Print URI before wrap: " + s);
        File f = new File(s);
        uri = f.toURI();
        // } catch (URISyntaxException e) {
        //TODO
        // DebugInfo.putErrorMessage("Error in path. No (back)slash found. Could not determine running path.");
        //   System.out.printf("Reason for failure %s\n",e.getReason());
        //     e.printStackTrace();
        // System.exit(-1);
        // }

        System.out.printf("Following path detected: %s\n", uri.getPath());

        //listFiles(uri.getPath());

        return uri.getPath();
    }

    private void listFiles(String path) {
        System.out.printf("Listing of %s\n", path);
        File[] list = new File(path).listFiles();
        for (File aList : list) {
            System.out.printf("  %s\n", aList.getName());
        }
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public String getConfigurationDirectory() {
        return rootDirectory + "/cfg/";
    }

    public String getPMMLDirectory() {
        return rootDirectory + "/pmml/";
    }

    public String getUIDataDirectory() {
        return rootDirectory + "/UIData/";
    }

    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public String findFile(String filename) {
        /*
            File is searched in following directories
                1/ user home directory
                2/ rootDirectory
                3/ rootDirectory + variants (cfg, pmml, UIData)
                4/ classpath via getResource
                5/ search using relative path
                6/ file search in rootDirectory and all subdirectories

         */

        String path;

        // 1/ User home directory
        path = System.getProperty("user.home") + fileSep + ".game" + fileSep + filename;
        System.out.printf("1/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }

        // 2/ rootDirectory
        path = getRootDirectory() + fileSep + filename;
        System.out.printf("2/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }

        // 3/ rootDirectory + variants (cfg, pmml, UIData)

        path = getRootDirectory() + fileSep + ".." + fileSep + filename;
        System.out.printf("3a/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }

        // cfg
        path = getRootDirectory() + fileSep + "cfg" + fileSep + filename;
        System.out.printf("3b/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }
        // pmml
        path = getRootDirectory() + fileSep + "pmml" + fileSep + filename;
        System.out.printf("3c/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }
        // UIData
        path = getRootDirectory() + fileSep + "UIData" + fileSep + filename;
        System.out.printf("3d/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }

        //4/ classpath via getResource
        URL url = this.getClass().getResource(filename);
        System.out.printf("4/ %s\n", url);
        if (url != null) {
            try {
                return (url.toURI().getPath());
            } catch (URISyntaxException e) {
                //TODO
                //DebugInfo.putErrorMessage("Path conversion failed. This should not happend. Defect path: " + url.getFile(),e);
                System.exit(-1);
            }
        }

        // relative addressing
        path = ".." + fileSep + "cfg" + fileSep + filename;
        System.out.printf("5a/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }
        path = "cfg" + fileSep + filename;
        System.out.printf("5b/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }

        path = filename;
        System.out.printf("5c/ %s\n", path);
        if (fileExists(path)) {
            return path;
        }
        // 6/ file search in rootDirectory and all subdirectories
        //TODO: Not implemented yet.
        //TODO
        //DebugInfo.putDebugMessage("Requested file " + filename + " was not found.", DebugInfo.DebugLevel.D_WARNING);
        return null;


    }

    private boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static FilePathLocator getInstance() {
        if (locatorPtr == null) {
            locatorPtr = new FilePathLocator();
        }

        return locatorPtr;
    }

    public static void main(String args[]) {
        FilePathLocator.getInstance().findFile("units.cfg");
    }
}
