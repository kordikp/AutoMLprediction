package game.trainers.gradient.numopt;

import game.trainers.gradient.numopt.events.IterationEvent;
import game.trainers.gradient.numopt.events.IterationListener;
import game.trainers.gradient.numopt.events.OptimizationEvent;
import game.trainers.gradient.numopt.events.OptimizationListener;

import javax.swing.event.EventListenerList;

/**
 * User: honza
 * Date: 18.2.2007
 * Time: 21:18:43
 */
public class MinimizationMethod {
    protected int iteration;

    protected double[] x;
    protected double fx;

    private EventListenerList listenerList = new EventListenerList();
    private IterationEvent iterationEvent;
    private OptimizationEvent optimizationEvent;

    public int getIteration() {
        return iteration;
    }

    public double[] getX() {
        return x;
    }

    public double getFx() {
        return fx;
    }

    public void addIterationListener(IterationListener l) {
        listenerList.add(IterationListener.class, l);
    }

    public void removeIterationListener(IterationListener l) {
        listenerList.remove(IterationListener.class, l);
    }

    public void addOptimizationListener(OptimizationListener l) {
        listenerList.add(OptimizationListener.class, l);
    }

    public void removeOptimizationListener(OptimizationListener l) {
        listenerList.remove(OptimizationListener.class, l);
    }

    protected void fireIterationStart() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == IterationListener.class) {
                // Lazily create the event:
                if (iterationEvent == null)
                    iterationEvent = new IterationEvent(this);
                ((IterationListener) listeners[i + 1]).IterationStart(iterationEvent);
            }
        }
    }

    protected void fireIterationEnd() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == IterationListener.class) {
                if (iterationEvent == null)
                    iterationEvent = new IterationEvent(this);
                ((IterationListener) listeners[i + 1]).IterationEnd(iterationEvent);
            }
        }
    }

    protected void fireOptimizationStart() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == OptimizationListener.class) {
                if (optimizationEvent == null)
                    optimizationEvent = new OptimizationEvent(this);
                ((OptimizationListener) listeners[i + 1]).OptimizationStart(optimizationEvent);
            }
        }
    }

    protected void fireOptimizationEnd() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == OptimizationListener.class) {
                if (optimizationEvent == null)
                    optimizationEvent = new OptimizationEvent(this);
                ((OptimizationListener) listeners[i + 1]).OptimizationEnd(optimizationEvent);
            }
        }
    }
}
