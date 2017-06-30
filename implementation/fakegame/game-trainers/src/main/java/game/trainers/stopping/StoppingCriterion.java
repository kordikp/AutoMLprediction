/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.trainers.stopping;


/**
 * this interface standardizes the stopping criterion for GMDH units learning
 */
public interface StoppingCriterion {
    /**
     */

    void init(int maxEp);

    void reset(int maxEp);

    void storeError(double error);

    void storeValError(double valError);

    double getBestValError();

    boolean stop();
}
