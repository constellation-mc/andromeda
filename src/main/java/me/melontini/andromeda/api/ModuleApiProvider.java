package me.melontini.andromeda.api;

import java.util.function.Consumer;
import java.util.function.Function;
import me.melontini.andromeda.bootstrap.ModuleManager;

public interface ModuleApiProvider {

  static ModuleApiProvider get() {
    return ModuleManager.get();
  }

  <I, O> void whenAvailable(ApiDeclaration<I, O> declaration, Consumer<Function<I, O>> consumer);
}
