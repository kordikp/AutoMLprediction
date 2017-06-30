package game.evolution.treeEvolution.evolutionControl;

import game.evolution.treeEvolution.HashTableContainer;
import game.evolution.treeEvolution.TreeNode;
import game.utils.Utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Implementation of file repository
 */
public class FileRepository implements DataRepository {
    private String dataFolder = "./evolution/metaData/";

    private File getFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void addNewRecord(File file, Integer id, String[] items) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(id.toString());
            for (int i = 0; i < items.length; i++) {
                bw.write("    " + items[i]);
            }
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void overwriteFile(File file, String[][] data) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            for (int i = 0; i < data.length; i++) {
                bw.write(data[i][0]);
                for (int j = 1; j < data[i].length; j++) {
                    bw.write("    " + data[i][j]);
                }
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMetaData(int id, Object[] metaData) {
        String[] strMetaData = convertToString(metaData);

        File file = getFile(dataFolder + "metaData.txt");
        addNewRecord(file, id, strMetaData);
    }

    public int saveMetaData(Object[] metaData) {
        String[] strMetaData = convertToString(metaData);

        File file = getFile(dataFolder + "metaData.txt");
        int maxId = 0;
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String[] records;
            int currentId;
            while ((strLine = br.readLine()) != null && !strLine.equals("")) {
                records = strLine.split("\\s++", 2);
                currentId = Integer.parseInt(records[0]);
                if (currentId > maxId) maxId = currentId;
            }
            maxId++;
            addNewRecord(file, maxId, strMetaData);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    public int updateMetaData(int oldId, Object[] newMetaData) {
        String[] strMetaData = convertToString(newMetaData);

        String[][] metaData = loadMetaData();
        String[] metaDataWithId = new String[strMetaData.length + 1];
        metaDataWithId[0] = Integer.toString(oldId);
        System.arraycopy(strMetaData, 0, metaDataWithId, 1, strMetaData.length);

        Integer maxId = 0;
        int oldIdOccurences = 0;
        int lineToChange = 0;
        int curId;
        for (int i = 0; i < metaData.length; i++) {
            curId = Integer.parseInt(metaData[i][0]);
            if (curId > maxId) maxId = curId;
            if (curId == oldId) {
                oldIdOccurences++;
                if (arrayEquality(metaData[i], metaDataWithId)) lineToChange = i;
            }

        }
        maxId++;

        if (oldIdOccurences <= 1) {
            return oldId;
        } else {
            File file = getFile(dataFolder + "metaData.txt");
            metaData[lineToChange][0] = maxId.toString();
            overwriteFile(file, metaData);
            return maxId;
        }
    }

    private String[] convertToString(Object[] array) {
        String[] strArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Integer) {
                strArray[i] = Integer.toString((Integer) array[i]);
            } else if (array[i] instanceof Double) {
                strArray[i] = Double.toString((Double) array[i]);
            } else {
                strArray[i] = (String) array[i];
            }
        }
        return strArray;
    }

    private boolean arrayEquality(Object[] a, Object[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!(a[i].equals(b[i]))) return false;
        }
        return true;
    }

    public void saveData(int id, FitnessContainer[] data) {
        File file = getFile(dataFolder + id + ".txt");

        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileStream);
            out.writeObject(data);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[][] loadMetaData() {
        File file = getFile(dataFolder + "metaData.txt");
        ArrayList<String[]> fileContent = new ArrayList<String[]>();

        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String[] items;
            while ((strLine = br.readLine()) != null) {
                if (strLine.equals("")) continue;
                items = strLine.split("\\s++");
                fileContent.add(items);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toArray(new String[][]{});
    }

    public FitnessContainer[] loadData(int id) {
        File file = getFile(dataFolder + id + ".txt");

        FitnessContainer[] data = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
            data = (FitnessContainer[]) oInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

}
