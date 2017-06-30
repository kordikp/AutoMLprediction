package game.evolution.treeEvolution.context;


import java.io.Serializable;
import java.util.ArrayList;

public class InterruptibleArrayList<E> extends ArrayList<E> implements Serializable {

    transient private Thread thread = Thread.currentThread();

    public InterruptibleArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public InterruptibleArrayList() {
        super();
    }

    public E get(int index) {
        if (thread.isInterrupted()) throw new ThreadDeath();
        return super.get(index);
    }

}
