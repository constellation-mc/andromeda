package me.melontini.andromeda.common.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.config.ConfigHandler;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

public final class DataConfigs extends IdentifiedJsonDataLoader {

  public static final Identifier DEFAULT = Andromeda.id("default");
  public static final ReloaderType<DataConfigs> RELOADER =
      ReloaderType.create(Andromeda.id("scoped_config"));

  public DataConfigs() {
    super(RELOADER.identifier());
  }

  public static DataConfigs get(MinecraftServer server) {
    try {
      return server.dm$getReloader(RELOADER);
    } catch (Exception e) {
      throw AndromedaException.builder()
          .cause(e)
          .report(false)
          .translatable("scoped_configs.no_reloader")
          .build();
    }
  }

  public Map<Identifier, Map<Module, Set<Data>>> configs;
  public Map<Module, Set<Data>> defaultConfigs;

  @Override
  protected void apply(
      Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
    Map<Identifier, Map<Module, Set<CompletableFuture<Data>>>> configs =
        new Object2ObjectOpenHashMap<>();
    Maps.transformValues(data, JsonElement::getAsJsonObject).forEach((id, object) -> {
      var m = ModuleManager.get()
          .getModule(id.getPath())
          .orElseThrow(() -> new IllegalStateException(
              "Invalid module path '%s'! The module must be enabled!".formatted(id.getPath())));
      var cls = m.getConfigDefinition(ConfigState.GAME).supplier().get();

      object.entrySet().forEach(entry -> {
        var map = configs.computeIfAbsent(
            new Identifier(entry.getKey()), string -> new Reference2ObjectOpenHashMap<>());
        map.computeIfAbsent(m, module -> new ReferenceLinkedOpenHashSet<>())
            .add(makeFuture(m, cls, entry.getValue()));
      });
    });

    Map<Identifier, Map<Module, Set<Data>>> parsed = new Object2ObjectOpenHashMap<>();
    CompletableFuture.allOf(configs.values().stream()
            .flatMap(map -> map.values().stream())
            .flatMap(Collection::stream)
            .toArray(CompletableFuture[]::new))
        .handle((unused, throwable) -> configs)
        .join()
        .forEach((identifier, moduleSetMap) -> {
          var n = parsed.computeIfAbsent(identifier, id -> new Object2ObjectOpenHashMap<>());
          moduleSetMap.forEach((module, completableFutures) -> {
            var set = n.computeIfAbsent(module, m -> new ReferenceLinkedOpenHashSet<>());
            completableFutures.forEach(future -> set.add(future.join()));
          });
        });
    defaultConfigs = parsed.remove(DEFAULT);
    this.configs = parsed;
  }

  private CompletableFuture<Data> makeFuture(
      Module m, Class<? extends Module.BaseConfig> cls, JsonElement element) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var parsed = Andromeda.GAME_HANDLER.parse(element, m);

            ReferenceOpenHashSet<Field> config = new ReferenceOpenHashSet<>();

            for (String field : element.getAsJsonObject().keySet()) {
              try {
                config.add(cls.getField(field));
              } catch (NoSuchFieldException e1) {
                throw new RuntimeException(
                    "Failed to load config data for module '%s'"
                        .formatted(m.meta().id()),
                    e1);
              }
            }
            return new Data(config, parsed);
          } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load config data for module '%s'".formatted(m.meta().id()), e);
          }
        },
        Util.getMainWorkerExecutor());
  }

  public record Data(Set<Field> cFields, Module.BaseConfig config) {}

  public void apply(ScopedConfigs.AttachmentGetter getter, Identifier identifier) {
    MakeSure.notNull(configs);

    ConfigHandler attachment = getter.andromeda$getConfigs();
    attachment.loadAll();
    attachment.forEach((entry, module) -> applyDataPacks(entry, module, identifier));

    if (Experiments.get().persistentScopedConfigs.isEmpty()) return;

    var manager = ModuleManager.get();
    for (String id : Experiments.get().persistentScopedConfigs) {
      attachment.save(manager
          .getModule(id)
          .orElseThrow(() -> new RuntimeException("No such module %s!".formatted(id))));
    }
  }

  private void apply(Module.BaseConfig config, Data data) {
    data.cFields().forEach((field) -> {
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

  void applyDataPacks(Module.BaseConfig config, Module m, Identifier id) {
    if (defaultConfigs != null) {
      var forModule = defaultConfigs.get(m);
      if (forModule != null) {
        for (Data data : forModule) apply(config, data);
      }
    }
    if (id.equals(DEFAULT)) return;

    var data = Objects.requireNonNull(configs).get(id);
    if (data != null) {
      var forModule = data.get(m);
      if (forModule != null) {
        for (Data data1 : forModule) apply(config, data1);
      }
    }
  }
}
