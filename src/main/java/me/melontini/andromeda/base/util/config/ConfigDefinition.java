package me.melontini.andromeda.base.util.config;

import java.util.function.Supplier;
import me.melontini.andromeda.base.Module;

public record ConfigDefinition<T extends Module.BaseConfig>(Supplier<Class<T>> supplier) {

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
