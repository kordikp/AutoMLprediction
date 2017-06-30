package game.trainers.gradient.numopt.events;

import game.trainers.gradient.numopt.MinimizationMethod;

/**
 * User: honza
 * Date: 19.2.2007
 * To change this template use File | Settings | File Templates.
 */
public class OptimizationEvent {
    private MinimizationMethod source;

    public OptimizationEvent(MinimizationMethod osource) {
        if (osource == null)
            throw new IllegalArgumentException("null source");

        this.source = osource;
    }

    public MinimizationMethod getSource() {
        return source;
    }

    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}
