package me.melontini.andromeda.base.events;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Function;

public class Bus<T> {

    private final Queue<T> listeners = new ArrayDeque<>();
    private final T invoker;

    public Bus(Function<Collection<T>, T> factory) {
        this.invoker = factory.apply(listeners);
    }

    public void listen(T listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public T invoker() {
        return this.invoker;
    }
}
