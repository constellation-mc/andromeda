package me.melontini.andromeda.common.util;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

// My least favorite thing from this mod.
// Consider this a mutable Optional.
public final class Keeper<T> {

  private volatile boolean initialized;
  private @Nullable T value;

  public static <T> Keeper<T> create() {
    return new Keeper<>();
  }

  public static <T> Keeper<T> now(T value) {
    Keeper<T> keeper = create();
    keeper.init(value);
    return keeper;
  }

  public boolean isPresent() {
    return value != null;
  }

  public void init(@Nullable T value) {
    if (initialized) return;

    synchronized (this) {
      if (initialized) return;
      this.value = value;
      this.initialized = true;
    }
  }

  public @Nullable T get() {
    return this.value;
  }

  public T orThrow() {
    return orThrow("No value present! Keeper not bootstrapped?");
  }

  public T orThrow(String msg) {
    return orThrow(() -> new IllegalStateException(msg));
  }

  public <X extends Throwable> T orThrow(Supplier<X> e) throws X {
    if (this.value == null) throw e.get();
    return this.value;
  }
}
