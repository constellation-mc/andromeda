package me.melontini.andromeda.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.bootstrap.config.ModConfigHandler;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.mixin.MixinHandler;
import me.melontini.andromeda.modules.ModuleDiscovery;
import me.melontini.andromeda.util.*;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

@CustomLog
@Accessors(fluent = true)
public class ModuleManager implements PreLaunchEntrypoint {

  @Getter
  private final ModContainer modContainer =
      FabricLoader.getInstance().getModContainer(AndromedaConstants.MODID).orElseThrow();

  @Getter
  private final ModConfigHandler modConfig = ModConfigHandler.load();

  @Getter
  private final MixinHandler mixinHandler = new MixinHandler(this);

  @Getter
  private final InstanceDataHolder dataHolder = InstanceDataHolder.load();

  private final Map<Class<?>, Module> modules = new IdentityHashMap<>();
  private final Map<String, Module> modulesByName = new LinkedHashMap<>();

  private static ModuleManager instance;

  @Override
  public void onPreLaunch() {
    instance = this;
    this.modConfig.save();
    VersionTracker.initialize(this);

    List<Class<? extends Module>> moduleClasses = new ModuleDiscovery()
        .discoverModules().stream()
            .peek(cls -> {
              if (ModuleHelper.getMeta(cls) == null)
                throw Util.create("%s has no ModuleInfo annotation", IllegalStateException::new);
            })
            .toList();

    for (Class<? extends Module> cls : moduleClasses) {
      try {
        // All modules must provide a single no-args constructor
        var ctx = cls.getDeclaredConstructors()[0];
        ctx.setAccessible(true);
        Module module = (Module) ctx.newInstance();
        this.modules.put(cls, module);
        this.modulesByName.put(ModuleHelper.id(module), module);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw Util.wrap("Failed to create module %s".formatted(cls.getName()), e);
      }
    }

    for (Module value : modulesByName.values()) {
      ModuleHelper.runAndDropBus(value, PostBootstrapEvent.ID, PostBootstrapEvent::postBootstrap);
    }

    // Inject all out mixin configs.
    this.mixinHandler.addMixins();
  }

  public <T extends Module> Optional<T> get(Class<T> cls) {
    return (Optional<T>) Optional.ofNullable(this.modules.get(cls));
  }

  public <T extends Module> Optional<T> get(String val) {
    return (Optional<T>) Optional.ofNullable(this.modulesByName.get(val));
  }

  public Collection<Module> loaded() {
    return modules.values();
  }

  public static ModuleManager get() {
    return instance;
  }

  private void printModuleStats() {
    Map<String, Set<Module>> categories = Utilities.supply(new LinkedHashMap<>(), map -> loaded()
        .forEach(m ->
            map.computeIfAbsent(m.meta().category(), s -> new LinkedHashSet<>()).add(m)));

    StringBuilder builder = new StringBuilder();
    categories.forEach((s, strings) -> {
      builder.append("\n\t - ").append(s).append("\n\t  |-- ");

      StringJoiner joiner = new StringJoiner(", ");
      strings.forEach(m -> joiner.add(ModuleHelper.dotted(m)));
      builder.append(joiner);
    });
    if (!categories.isEmpty()) {
      log.info("Loading {} modules: {}", loaded().size(), builder);
    } else {
      log.info("No modules loaded!");
    }
  }
}
