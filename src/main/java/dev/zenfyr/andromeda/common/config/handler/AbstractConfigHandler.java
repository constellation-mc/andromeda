package dev.zenfyr.andromeda.common.config.handler;

import static dev.zenfyr.andromeda.bootstrap.AndromedaConstants.MODID;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.EventMarker;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.andromeda.common.Andromeda;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class AbstractConfigHandler {

  private final Map<ConfigDefinition<?>, BaseConfig> configs = new IdentityHashMap<>();
  private final Map<ConfigDefinition<?>, BaseConfig> defaultConfigs = new IdentityHashMap<>();

  private final Path basePath;
  private final Map<Module, ConfigDefinition<?>> definitions = new LinkedHashMap<>();

  @Getter
  protected final Gson gson;

  public AbstractConfigHandler(
      ModuleManager manager, Path path, EventMarker<RegisterConfigEvent> id) {
    this.basePath = path;
    this.gson = Andromeda.buildGson();

    for (Module module : this.modules(manager)) {
      var bus = module.getOrCreateBus(id, null);
      if (bus == null) continue;

      var def = bus.invoker().onRegisterConfigs();
      if (def == null) continue;

      var old = this.definitions.put(module, def);
      if (old != null) throw new IllegalStateException();
    }
  }

  protected abstract Collection<Module> modules(ModuleManager manager);

  public Path resolve(Module module) {
    return this.basePath.resolve(MODID + "/" + ModuleHelper.id(module) + ".json");
  }

  public <T extends BaseConfig> ConfigDefinition<T> getDefinition(Module module) {
    return (ConfigDefinition<T>) this.definitions.get(module);
  }

  public <T extends BaseConfig> T get(ConfigDefinition<T> module) {
    return (T) this.configs.get(module);
  }

  public void forEach(BiConsumer<Module, BaseConfig> consumer) {
    this.definitions.forEach((module, definition) -> consumer.accept(module, this.get(definition)));
  }

  // Allow implementors to pull default configs from root.
  protected <T extends BaseConfig> T createDefault(ConfigDefinition<T> module) {
    try {
      return module.supplier().get().getConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw Util.wrap(
          "Failed to default new config from invalid config definition (%s)."
              .formatted(module.supplier().get().getName()),
          e);
    }
  }

  public <T extends BaseConfig> T getDefault(ConfigDefinition<T> module) {
    var entry = (T) this.defaultConfigs.get(module);
    if (entry == null) {
      synchronized (this.defaultConfigs) {
        entry = this.createDefault(module);
        this.defaultConfigs.put(module, entry);
      }
    }
    return entry;
  }

  protected abstract void writeToFile(Path path, BaseConfig entry) throws IOException;

  public void save(Module module) {
    if (!this.definitions.containsKey(module))
      throw new IllegalStateException(ModuleHelper.id(module));
    var path = resolve(module);
    var entry = get(this.getDefinition(module));

    synchronized (this.getDefinition(module)) {
      try {
        this.writeToFile(path, entry);
      } catch (IOException e) {
        throw Util.wrap("Failed to save config to file (%s)".formatted(path), e);
      }
    }
  }

  // Despite its name, this method is not exactly "createNewConfig". It's used in `load` and nowhere
  // else.
  protected <T extends BaseConfig> T createNewConfig(
      Module module, ConfigDefinition<T> definition) {
    try {
      return definition.supplier().get().getConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw Util.wrap(
          "Failed to create new config for '%s' from invalid config definition."
              .formatted(ModuleHelper.id(module)),
          e);
    }
  }

  protected abstract BaseConfig readFromFile(Path path, ConfigDefinition<?> definition)
      throws IOException;

  public BaseConfig load(Module module) {
    if (!this.definitions.containsKey(module))
      throw new IllegalStateException(ModuleHelper.id(module));
    var definition = this.getDefinition(module);
    var path = resolve(module);

    if (!Files.exists(path)) return this.createNewConfig(module, definition);

    synchronized (definition) {
      try {
        return readFromFile(path, definition);
      } catch (IOException e) {
        throw Util.wrap("Failed to read config %s from file".formatted(path), e);
      }
    }
  }

  public void saveAll() {
    CompletableFuture.allOf(this.definitions.keySet().stream()
            .map(module -> CompletableFuture.runAsync(() -> this.save(module)))
            .toArray(CompletableFuture[]::new))
        .join();
  }

  public void loadAll() {
    Map<ConfigDefinition<?>, CompletableFuture<BaseConfig>> configs = new IdentityHashMap<>();
    for (Map.Entry<Module, ConfigDefinition<?>> entry : this.definitions.entrySet()) {
      configs.put(entry.getValue(), CompletableFuture.supplyAsync(() -> this.load(entry.getKey())));
    }
    synchronized (this.configs) {
      this.configs.putAll(Maps.transformValues(configs, CompletableFuture::join));
    }
  }
}
