package game.trainers.gradient.Marquardt;

/**
 * <dl>
 * <p>
 * <dt>Purpose:
 * <p>
 * <dd>Exception class used in Marquardt Minimiser.
 * <p>
 * <dt>Description:
 * <p>
 * <dd>Standard extension of the Exception class.
 * <p>
 * </dl>
 *
 * @author Danny Alexander
 * @version $Id: MarquardtMinimiserException.java,v 1.3 2005/08/18 11:13:57
 *          ucacmgh Exp $
 */
public class MarquardtMinimiserException extends Exception {

    public MarquardtMinimiserException() {
        super();
    }

    public MarquardtMinimiserException(String s) {
        super(s);
    }
}

