package game.trainers.gradient.numopt.events;

import java.util.EventListener;

/**
 * User: honza
 * Date: 19.2.2007
 * Time: 16:39:52
 */
public interface OptimizationListener extends EventListener {
    public void OptimizationStart(OptimizationEvent oie);

    public void OptimizationEnd(OptimizationEvent oie);
}
