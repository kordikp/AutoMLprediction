package game.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

public class FileGameData extends AbstractGameData {

    private final static String OUTPUT_FEATURE_ID = "!";

    public FileGameData(String fileName) {
        readDataFile(fileName);
    }

    protected String readDataFile(String fileName) {
        String line, number, name;
        FileReader fr = null;
        try {
            if (fr == null) {
                fr = new FileReader(fileName);
            }
            BufferedReader sourceFile = new BufferedReader(fr);


            int what = 0;
            while ((line = sourceFile.readLine()) != null) {
                StringTokenizer cut = new StringTokenizer(line);
                while (cut.hasMoreTokens()) {
                    number = cut.nextToken();
                    switch (what) {
                        case 0:
                            if (number.startsWith(OUTPUT_FEATURE_ID)) {
                                name = number.substring(1);
                                createOutputAttribute(name, 0, 0, true, 0);
                            } else {
                                if (number.length() > 0) {
                                    createInputFactor(number, 0, 0, 0, true);
                                }
                            }
                            break;
                        case 1: //groups
                            name = "g" + Integer.toString(getGroups()); //name
                            if (getONumber() < 1) {
                                createOutputAttribute(getIFactor().get(getINumber() - 1).name, 0, 0, true, 0);
                                deleteInputFactor(getIFactor().get(getINumber() - 1).name);
                            }
                            int ii = 1;
                            try { // if instNumber name missing
                                getIFactor().get(0).setValue(Double.valueOf(number));

                            } catch (NumberFormatException e) {
                                if (number.compareTo("?") == 0)
                                    getIFactor().get(0).setValue(Double.NaN);
                                else {
                                    name = number;
                                    ii = 0;
                                }
                            }
                            for (; ii < getINumber(); ii++) { //ifact[i]
                                if (cut.hasMoreTokens()) {
                                    number = cut.nextToken();
                                } else {
                                    //TODO vyhodit vyjimku.
                                    break;
                                }
                                try {
                                    getIFactor().get(ii).setValue(Double.valueOf(number));
                                } catch (NumberFormatException e) {
                                    if (number.compareTo("NaN") == 0)
                                        getIFactor().get(ii).setValue(Double.
                                                NaN);
                                    else {
                                        //System.out.println(number + "->NaN");
                                    }
                                    getIFactor().get(ii).setValue(Double.NaN);
                                }
                            }

                            if (!cut.hasMoreTokens()) {
                                if (getGroups() == 0)
                                    System.out.println("Target values of output variables are missing -> replaced by zeroes");

                                for (int i = 0; i < getONumber(); i++) {
                                    getOAttr().get(i).val = 0;
                                }
                            } else {

                                for (int i = 0; i < getONumber(); i++) { //oattrs[i]
                                    if (cut.hasMoreTokens()) {
                                        number = cut.nextToken();
                                    } else {
                                        //TODO
                                        break;
                                    }
                                    getOAttr().get(i).val = Double.valueOf(number);
                                }
                            }
                            double[] oattr = new double[getONumber()];
                            double[] iattr = new double[getINumber()];

                            for (int i = 0; i < getONumber(); i++) { //oattrs[i]
                                oattr[i] = getOAttr().get(i).getValue();
                            }

                            for (int i = 0; i < getINumber(); i++) { //oattrs[i]
                                iattr[i] = getIFactor().get(i).getValue();
                            }

                            //nastaveni instance.
                            setInstance(name, iattr, oattr);

                            while (cut.hasMoreTokens()) {
                                number = cut.nextToken();
                            }
                            break;
                    }
                }
                what = 1;
            }

            fr.close();
            //TODO zjistit.
            //TODO musi se dodelat, aby se prepocitali data.
            //countRanges();
            refreshDataVectors();

            return (Integer.toString(getGroups()) + " game.data vectors was successfully read from file " + fileName + " ");
        } catch (java.io.IOException ioe) {
            String badFile = fileName;
            ioe.printStackTrace();
            return ("Error occured while reading <br> input file (" + badFile + ")");
        } catch (java.lang.NullPointerException npe) {
            npe.printStackTrace();
            return ("Error: something is wrong <br> vectors restored:" + Integer.toString(getGroups()));
        } catch (java.lang.NumberFormatException ioe) {
            ioe.printStackTrace();
            String badFile = fileName;
            return ("Error occured while reading " + badFile + "<br> (number format exception)");
        }
    }


}
