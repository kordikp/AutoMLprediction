package game.trainers.pso;

import game.trainers.gradient.Newton.Uncmin_methods;

public class Hejno {
    private int pocetPtaku;

    private int pocetIteraci = 1;

    private int maxPocetIteraci = 100;

    private double minAccetableError = 0;
    private static Uncmin_methods trainer;
    int rozmer;

    private Ptak[] ptak;

    public Hejno(Uncmin_methods train, int dimensions, double velikost_dimenzi) {
        trainer = train;
        pocetPtaku = 10;
        ptak = new Ptak[pocetPtaku];
        double chyba;
        Ptak.firstInit(trainer, 20.0d, 20.0d, dimensions, velikost_dimenzi, 1.0 / 2.0);
        for (int i = 0; i < pocetPtaku; i++) {
            ptak[i] = new Ptak();
        }

        // Okynko moje = new Okynko(dimensions, velikost_dimenzi, ptak);
        // moje.setVisible(true);
        do {
            Ptak.gBestError = Double.POSITIVE_INFINITY;
            for (int i = 0; i < pocetPtaku; i++) {
                ptak[i].countErrors();
            }
            chyba = 0.0;
            for (int i = 0; i < pocetPtaku; i++) {
                ptak[i].newVelocityAndPosition();
                chyba = +ptak[i].presentError * ptak[i].presentError;
            }
            chyba = Math.sqrt(chyba) / pocetPtaku;

            //System.out.println("Iterace: " + pocetIteraci + "; chyba: " + chyba);
        } while ((pocetIteraci++ < maxPocetIteraci) && (chyba > minAccetableError));
    }

    public double getBest(int i) {
        return Ptak.getgBest(i);
    }
}
