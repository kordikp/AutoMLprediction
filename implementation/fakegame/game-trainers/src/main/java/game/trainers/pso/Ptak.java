package game.trainers.pso;

import game.trainers.gradient.Newton.Uncmin_methods;

class Ptak {
    //   --staticke promenne
    static private int dimensions; //pocet prvku ve vektoru

    private static double[] gBest; //nejlepsi pozice ze vsech ptaku v historii

    static public double gBestError; //chyba z nelepsi pozice

    static private double c1; //ucici konstanta

    static private double c2; //ucici konstanta

    static private double velikost_dimenzi; //rozmer dimenzi - proinicializaci
    // pozice ptaku

    static private double max_v; //maximalni povolena rychlost ptacku
    private static Uncmin_methods trainer;
    //   --instancni promenne
    private double pBestError; //vyska = chyba u nejlepsi pozice ptaka

    private double pBest[]; //nejlepsi pozice ptaka v historii

    public double presentError;

    public double present[]; //pozice ptaka

    private double[] v;

    public static void firstInit(Uncmin_methods train, double c1, double c2, int dimensions, double velikost_dimenzi, double max_v) {
        trainer = train;
        Ptak.dimensions = dimensions;
        Ptak.velikost_dimenzi = velikost_dimenzi;
        Ptak.max_v = Math.abs(max_v); //zajisteni zadani kladne maximalni
        // dovolene rychlosti
        Ptak.c1 = c1;
        Ptak.c2 = c2;
        gBest = new double[dimensions];
        gBestError = Double.POSITIVE_INFINITY;
    }

    public static double getgBestError() {
        return gBestError;
    }

    public static double getgBest(int i) {
        return gBest[i];
    }

    public Ptak() {
        present = new double[dimensions];
        pBest = new double[dimensions];
        v = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            present[i] = (Math.random() * velikost_dimenzi) - velikost_dimenzi / 2.0d; //vygenerovani nahodne pozice ptaka v rozmezi
            // -velikost_dimenzi/2 az +velikost_dimenzi/2
            pBest[i] = present[i]; //inicializovana pozice je zaroven nejlepsi
            // dosazenou
            v[i] = (Math.random() * max_v) - max_v / 2.0d;
        }
        pBestError = Double.POSITIVE_INFINITY;
    }

    private double getError(double[] present) {
        return trainer.f_to_minimize(present);
    }

    public void countErrors() {
        presentError = getError(present);
        if (presentError < pBestError) {
            pBestError = presentError;
            System.arraycopy(present, 0, pBest, 0, dimensions);
            if (presentError < gBestError) {
                gBestError = presentError;
                System.arraycopy(present, 0, gBest, 0, dimensions);
            }
        }
    }

    public void newVelocityAndPosition() {
        for (int i = 0; i < dimensions; i++) {
            v[i] = v[i] + c1 * Math.random() * (pBest[i] - present[i]) + c2 * Math.random() * (gBest[i] - present[i]);
            if (v[i] > max_v) //kontrola prekroceni maximalni dovolene
                // rychlosti
                v[i] = max_v;
            else if (v[i] < -max_v) v[i] = -max_v;
        }
        for (int i = 0; i < dimensions; i++) {
            present[i] = present[i] + v[i];
        }
    }
}
