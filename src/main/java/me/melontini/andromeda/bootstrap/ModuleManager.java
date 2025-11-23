package me.melontini.andromeda.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.bootstrap.config.handler.BootstrapConfigHandler;
import me.melontini.andromeda.bootstrap.config.handler.ModConfigHandler;
import me.melontini.andromeda.bootstrap.event.BootstrapConfigEvent;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.event.PostModuleInitEvent;
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
  private final BootstrapConfigHandler configHandler = new BootstrapConfigHandler();

  @Getter
  private final MixinHandler mixinHandler = new MixinHandler(this);

  @Getter
  private final InstanceDataHolder dataHolder = InstanceDataHolder.load();

  private final Map<Class<?>, Module> discoveredModules = new IdentityHashMap<>();
  private final Map<String, Module> discoveredModulesByName = new LinkedHashMap<>();

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
        this.discoveredModules.put(cls, module);
        this.discoveredModulesByName.put(ModuleHelper.id(module), module);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw Util.wrap("Failed to create module %s".formatted(cls.getName()), e);
      }
    }

    // Post module init event. All discovered modules are available here. Can be used to fix configs
    // and whatnot.
    for (Module value : discoveredModulesByName.values()) {
      ModuleHelper.runAndDropBus(
          value, PostModuleInitEvent.ID, PostModuleInitEvent::postModuleInit);
    }

    for (Module value : discoveredModulesByName.values()) {
      var config = this.configHandler.load(value);

      // Allow modules to modify their own configs.
      // Other modules can subscribe to this event, but this is not correct.
      ModuleHelper.runAndDropBus(
          value, BootstrapConfigEvent.ID, event -> event.bootstrapConfig(config));

      if (config.enabled || Debug.get().enableAllModules) {
        this.modules.put(value.getClass(), value);
        this.modulesByName.put(ModuleHelper.id(value.meta()), value);
      }
      this.configHandler.save(value);
    }

    // Inject all out mixin configs.
    this.mixinHandler.addMixins();

    this.printModuleStats();

    // All modules must be available by this point.
    for (Module value : modulesByName.values()) {
      ModuleHelper.runAndDropBus(value, PostBootstrapEvent.ID, PostBootstrapEvent::postBootstrap);
    }
  }

  public <T extends Module> Optional<T> getDiscovered(Class<T> cls) {
    return (Optional<T>) Optional.ofNullable(this.discoveredModules.get(cls));
  }

  public <T extends Module> Optional<T> getDiscovered(String val) {
    return (Optional<T>) Optional.ofNullable(this.discoveredModulesByName.get(val));
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

  public Collection<Module> all() {
    return discoveredModules.values();
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
