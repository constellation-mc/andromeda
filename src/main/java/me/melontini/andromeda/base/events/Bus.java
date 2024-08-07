package me.melontini.andromeda.base.events;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Bus<T> {

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

  /**
   * Removes all listeners for an event.
   */
  public void drop() {
    synchronized (this) {
      this.listeners.clear();
    }
  }

  /**
   * Invokes the event using a consumer
   * and drops all listeners immediately after.
   */
  public void invokeAndDrop(Consumer<T> invoker) {
    invoker.accept(this.invoker());
    this.drop();
  }
}
