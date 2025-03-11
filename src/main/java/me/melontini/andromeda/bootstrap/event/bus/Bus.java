package me.melontini.andromeda.bootstrap.event.bus;

import java.util.function.Function;

// There are two types of busses in Andromeda: per-module and monolithic.
// Per-module buses are used during bootstrap to filter-out unloaded modules.
// Those buses should be subscribed to in the module constructor.
// Monolithic buses are used after bootstrap.
public interface Bus<T> {

  static <T> Bus<T> create(Class<T> type, Function<T[], T> factory) {
    return new BusImpl<>(type, factory);
  }

  void listen(T listener);

  T invoker();
}
