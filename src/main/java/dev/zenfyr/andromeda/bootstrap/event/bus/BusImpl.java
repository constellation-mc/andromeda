package dev.zenfyr.andromeda.bootstrap.event.bus;

import java.lang.reflect.Array;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;

public class BusImpl<T> implements Bus<T> {

  private T[] listeners;
  private T invoker;
  private final Function<T[], T> factory;

  BusImpl(Class<T> type, Function<T[], T> factory) {
    this.factory = factory;
    this.listeners = (T[]) Array.newInstance(type, 0);
    this.rebuildInvoker();
  }

  private void rebuildInvoker() {
    this.invoker = this.factory.apply(this.listeners);
  }

  public void listen(T listener) {
    synchronized (this) {
      this.listeners = ArrayUtils.add(listeners, listener);
      this.rebuildInvoker();
    }
  }

  public T invoker() {
    return this.invoker;
  }
}
