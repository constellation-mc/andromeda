package dev.zenfyr.andromeda.bootstrap.config;

import java.util.function.Supplier;
import dev.zenfyr.andromeda.common.config.GameConfig;

// Used to type-hint and provide implementation types to GSON.
// The class is wrapped in a supplier to avoid classloading too early.
public final class ConfigDefinition<C extends BaseConfig> {
  private final Supplier<Class<C>> supplier;

  public static ConfigDefinition<BaseConfig> base() {
    return create(() -> BaseConfig.class);
  }

  public static ConfigDefinition<GameConfig> game() {
    return create(() -> GameConfig.class);
  }

  public static <C extends BaseConfig> ConfigDefinition<C> create(Supplier<Class<C>> supplier) {
    return new ConfigDefinition<>(supplier);
  }

  public ConfigDefinition(Supplier<Class<C>> supplier) {
    this.supplier = supplier;
  }

  public Supplier<Class<C>> supplier() {
    return supplier;
  }
}
