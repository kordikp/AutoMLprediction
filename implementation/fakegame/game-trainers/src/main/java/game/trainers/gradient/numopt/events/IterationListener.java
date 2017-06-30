package game.trainers.gradient.numopt.events;

import java.util.EventListener;

/**
 * User: honza
 * Date: 18.2.2007
 * Time: 21:23:47
 */
public interface IterationListener extends EventListener {
    public void IterationStart(IterationEvent oie);

    public void IterationEnd(IterationEvent oie);
}
