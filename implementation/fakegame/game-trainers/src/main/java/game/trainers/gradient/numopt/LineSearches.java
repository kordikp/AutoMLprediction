package game.trainers.gradient.numopt;

/**
 * User: drchaj1
 * Date: 29.3.2007
 * Time: 14:38:43
 */
class LineSearches {
    public static boolean checkWolfeSuffcientDecrease(double ofx0, double oslope0, double oalpha, double ofAlpha, double oc1) {
        return ofAlpha <= ofx0 + oalpha * oc1 * oslope0;
    }

    public static boolean checkStrongWolfeCurvature(double oslope0, double oslopeAlpha, double oc2) {
        return Math.abs(oslopeAlpha) <= -oc2 * oslope0;
    }

}
