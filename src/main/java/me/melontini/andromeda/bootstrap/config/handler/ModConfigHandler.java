package me.melontini.andromeda.bootstrap.config.handler;

import static me.melontini.andromeda.util.AndromedaConstants.MODID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import lombok.CustomLog;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ModInitConfig;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.Util;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public class ModConfigHandler {

  private static final List<Key<?>> KEYS = new ArrayList<>();
  private static final Path PATH =
      FabricLoader.getInstance().getConfigDir().resolve(MODID + "/mod.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  static {
    KEYS.add(ModInitConfig.KEY);

    KEYS.add(Debug.KEY);
  }

  private final Map<Key<?>, BaseConfig> configs = new IdentityHashMap<>();

  public static ModConfigHandler load() {
    ModConfigHandler config = new ModConfigHandler();
    try {
      if (Files.exists(PATH)) {
        try (var reader = Files.newBufferedReader(PATH)) {
          JsonObject json = GSON.fromJson(reader, JsonObject.class);
          for (Key<?> key : KEYS) {
            if (json.has(key.key())) {
              config.configs.put(key, GSON.fromJson(json.get(key.key()), key.type()));
              continue;
            }
            config.configs.put(key, (BaseConfig) key.type().getConstructors()[0].newInstance());
          }
        }
      } else {
        for (Key<?> key : KEYS) {
          config.configs.put(key, (BaseConfig) key.type().getConstructors()[0].newInstance());
        }
      }
    } catch (IOException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw Util.create("Failed to load main mod config!");
    }
    return config;
  }

  public void save() {
    try {
      var parent = PATH.getParent();
      if (parent != null) Files.createDirectories(parent);

      JsonObject object = new JsonObject();
      for (Key<?> key : KEYS) {
        object.add(key.key(), GSON.toJsonTree(this.configs.get(key), key.type()));
      }
      Files.writeString(PATH, GSON.toJson(object));
    } catch (IOException e) {
      Util.throwIfDev(() -> Util.wrap("Failed to save main mod config!", e));
      log.error("Failed to save main mod config!", e);
    }
  }

  public <C extends BaseConfig> C get(Key<C> key) {
    return (C) this.configs.get(key);
  }

  public record Key<C extends BaseConfig>(String key, Class<C> type) {
    @Override
    public boolean equals(Object obj) {
      return obj == this;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }
  }
}
