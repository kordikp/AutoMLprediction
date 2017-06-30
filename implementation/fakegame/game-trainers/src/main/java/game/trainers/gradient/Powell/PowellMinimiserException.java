package game.trainers.gradient.Powell;

/**
 * <dl>
 * <dt>Description:
 * <dd>Minimises a function objfunc of n variables.
 * <dl>
 *
 * @author Kathleen Curran $Id: PowellMinimiserException.java,v 1.1 2005/07/25
 *         15:40:18 ucacpco Exp $
 */
class PowellMinimiserException extends Exception {
    PowellMinimiserException() {
        super();
    }

    PowellMinimiserException(String s) {
        super(s);
    }
}
