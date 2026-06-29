package dev.zenfyr.andromeda.common.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.GameConfigHandler;
import dev.zenfyr.andromeda.common.util.IdentifiedJsonDataLoader;
import dev.zenfyr.pulsar.api.resources.ReloadListenerType;
import dev.zenfyr.pulsar.api.resources.ServerReloadListenersEvent;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.CustomLog;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

// Loads and applies custom config overrides from data packs
@CustomLog
public final class DataConfigs extends IdentifiedJsonDataLoader {

  public static final ResourceLocation DEFAULT = Andromeda.id("default");
  public static final ReloadListenerType<DataConfigs> RELOADER =
      ReloadListenerType.create(Andromeda.id("scoped_config"));

  public static DataConfigs get(MinecraftServer server) {
    return server.pulsar$getReloadListener(RELOADER);
  }

  private final ModuleManager moduleManager;
  public Map<ResourceLocation, Map<Module, Set<Data>>> configs;
  public Map<Module, Set<Data>> defaultConfigs;

  public DataConfigs(ModuleManager moduleManager) {
    super(RELOADER.location());
    this.moduleManager = moduleManager;
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
    Map<ResourceLocation, Map<Module, Set<Data>>> parsed = new HashMap<>();

    for (var entry : Maps.transformValues(data, JsonElement::getAsJsonObject).entrySet()) {
      ResourceLocation id = entry.getKey();
      JsonObject json = entry.getValue();
      // Modules must be loaded to apply their configs.
      var module = this.moduleManager
          .get(id.getPath())
          .orElseThrow(() -> new IllegalStateException(
              "Invalid module path '%s'! The module must be enabled!".formatted(id.getPath())));
      var type = Andromeda.GAME.getDefinition(module).supplier().get();

      Maps.transformValues(json.asMap(), JsonElement::getAsJsonObject).forEach((string, value) -> {
        var dimension = new ResourceLocation(string);
        var cfg = Andromeda.GAME.gson().fromJson(value, type);

        // Parse the fields that must be modified during `apply`
        Set<Field> overrides = new ReferenceOpenHashSet<>();
        for (String field : value.keySet()) {
          try {
            overrides.add(type.getField(field));
          } catch (NoSuchFieldException e) {
            throw Util.wrap("No such field '%s' for module %s".formatted(field, id), e);
          }
        }
        // We store configs grouped by dimension.
        parsed
            .computeIfAbsent(dimension, i_ -> new HashMap<>())
            .computeIfAbsent(module, m_ -> new ReferenceLinkedOpenHashSet<>())
            .add(new Data(overrides, cfg));
      });
    }

    this.defaultConfigs = parsed.remove(DEFAULT);
    this.configs = parsed;
  }

  public void applyConfigs(AttachmentGetter getter, ResourceLocation dimension) {
    Objects.requireNonNull(configs);

    var handler = getter.andromeda$getConfigs();
    handler.loadAll();
    handler.forEach((module, baseConfig) -> this.applyDataPacks(baseConfig, module, dimension));
  }

  void applyDataPacks(BaseConfig config, Module module, ResourceLocation dimension) {
    if (defaultConfigs != null) {
      var forModule = defaultConfigs.get(module);
      if (forModule != null) for (Data data : forModule) this.apply(config, data);
    }
    if (dimension.equals(DEFAULT)) return;

    var overrides = Objects.requireNonNull(configs).get(dimension);
    if (overrides != null) {
      var forModule = overrides.get(module);
      if (forModule != null) for (Data data1 : forModule) apply(config, data1);
    }
  }

  private void apply(BaseConfig config, Data data) {
    data.cFields().forEach(field -> {
      try {
        field.set(config, field.get(data.config()));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(
            "Failed to apply config data for module '%s'"
                .formatted(config.getClass().getSimpleName()),
            e);
      }
    });
  }

  public record Data(Set<Field> cFields, BaseConfig config) {}

  public interface WorldExtension {
    default <T extends BaseConfig> T am$get(ConfigDefinition<T> definition) {
      throw new IllegalStateException("Game configs requested in client world '%s'!"
          .formatted(((Level) this).dimension().location()));
    }
  }

  public interface AttachmentGetter {
    GameConfigHandler andromeda$getConfigs();
  }

  public static void init(ModuleManager manager) {
    ServerReloadListenersEvent.EVENT.listen(
        context -> context.register(RELOADER.location(), new DataConfigs(manager)));

    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
      if (!success) return;

      var configs = DataConfigs.get(server);
      for (ServerLevel world : server.getAllLevels()) {
        configs.applyConfigs((AttachmentGetter) world, world.dimension().location());
      }
    });
  }
}
