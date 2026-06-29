package dev.zenfyr.andromeda.bootstrap.config.handler;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.config.BootstrapConfig;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.pulsar.api.platform.Platform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.CustomLog;

// Loads and saves bootstrap configs.
// This handler is different to the AbstractConfigHandler's in the sense that it must not rely on
// Config Definitions to identify configs.
@CustomLog
public final class BootstrapConfigHandler {

  private final Path basePath = Platform.getPlatform().getConfigDir().resolve(MODID);
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final Map<Class<?>, BootstrapConfig> configs = new IdentityHashMap<>();

  public BootstrapConfig get(Module module) {
    return this.configs.get(module.getClass());
  }

  private BootstrapConfig loadImpl(Module module) {
    Path path = basePath.resolve(ModuleHelper.id(module) + ".json");
    if (!Files.exists(path)) return BootstrapConfig.create(module);

    try (var reader = Files.newBufferedReader(path)) {
      JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
      if (object.has("bootstrap")) object = object.getAsJsonObject("bootstrap");
      return Objects.requireNonNull(gson.fromJson(object, BootstrapConfig.class));
    } catch (IOException e) {
      throw Util.create("Failed to load %s!".formatted(path));
    }
  }

  public BootstrapConfig load(Module module) {
    var cfg = this.loadImpl(module);
    this.configs.put(module.getClass(), cfg);
    return cfg;
  }

  public void save(Module module) {
    if (!this.configs.containsKey(module.getClass()))
      throw new IllegalStateException(
          "Config for module %s is not loaded".formatted(ModuleHelper.id(module)));
    Path path = basePath.resolve(ModuleHelper.id(module) + ".json");

    JsonObject object = new JsonObject();
    if (Files.exists(path)) {
      try (var reader = Files.newBufferedReader(path)) {
        object = JsonParser.parseReader(reader).getAsJsonObject();
      } catch (IOException | RuntimeException e) {
        Util.throwIfDev(() -> new RuntimeException("Failed to load %s!".formatted(path), e));
        object = new JsonObject();
      }
    }

    var o = gson.toJsonTree(get(module)).getAsJsonObject();
    if (!object.has("bootstrap")) o.asMap().keySet().forEach(object::remove);
    object.add("bootstrap", o);

    try {
      var parent = path.getParent();
      if (parent != null) Files.createDirectories(parent);
      Files.writeString(path, gson.toJson(object));
    } catch (IOException e) {
      throw Util.create("Failed to save %s!".formatted(path));
    }
  }
}
