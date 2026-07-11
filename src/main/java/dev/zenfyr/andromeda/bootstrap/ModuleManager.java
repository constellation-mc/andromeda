package dev.zenfyr.andromeda.bootstrap;

import dev.zenfyr.andromeda.bootstrap.config.ConnectionsConfig;
import dev.zenfyr.andromeda.bootstrap.config.DebugConfig;
import dev.zenfyr.andromeda.bootstrap.config.ModInitConfig;
import dev.zenfyr.andromeda.bootstrap.config.handler.BootstrapConfigHandler;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import dev.zenfyr.andromeda.bootstrap.event.BootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.ModuleLoadStateEvent;
import dev.zenfyr.andromeda.bootstrap.event.ModuleLoadStateEvent.ForcedState;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.event.PostModuleInitEvent;
import dev.zenfyr.andromeda.bootstrap.util.*;
import dev.zenfyr.andromeda.bootstrap.util.mixin.AndromedaMixinPlugin;
import dev.zenfyr.andromeda.modules.ModuleDiscovery;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import dev.zenfyr.pulsar.api.platform.Platform;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.*;
import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;

@CustomLog
@Accessors(fluent = true)
public class ModuleManager {

  private static final Object INIT_LOCK = new Object();

  @Getter
  private final ModConfigHandler modConfig;

  @Getter
  private final ModInitConfig modInitConfig;

  @Getter
  private final ConnectionsConfig connections;

  @Getter
  private final DebugConfig debug;

  @Getter
  private final BootstrapConfigHandler configHandler = new BootstrapConfigHandler();

  @Getter
  private final InstanceDataHolder dataHolder = InstanceDataHolder.load();

  private final Map<Class<?>, Module> discoveredModules = new IdentityHashMap<>();
  private final Map<String, Module> discoveredModulesByName = new LinkedHashMap<>();

  private final Map<Class<?>, Module> modules = new IdentityHashMap<>();
  private final Map<String, Module> modulesByName = new LinkedHashMap<>();
  private final Map<String, Module> moduleByMixinPkg = new HashMap<>();

  private Throwable delayed;

  private static ModuleManager instance;

  public ModuleManager() {
    this.modConfig = ModConfigHandler.load();
    this.modInitConfig = this.modConfig().get(ModInitConfig.KEY);
    this.connections = this.modConfig().get(ConnectionsConfig.KEY);
    this.debug = this.modConfig().get(DebugConfig.KEY);
  }

  public void onInitialize() {
    if (!Files.exists(Util.HIDDEN_PATH)) {
      try {
        Files.createDirectories(Util.HIDDEN_PATH);
        if (Util.HIDDEN_PATH.getFileSystem().supportedFileAttributeViews().contains("dos"))
          Files.setAttribute(
              Util.HIDDEN_PATH, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    if (Launchpad.isLaunchpad()) {
      log.warn("Andromeda is loading via Launchpad! Here be dragons!");
    }

    this.modConfig().save();
    this.connections().initialize(this);

    List<Class<? extends Module>> moduleClasses = new ModuleDiscovery()
        .discoverModules().stream()
            .peek(cls -> {
              if (ModuleHelper.getMeta(cls) == null)
                throw Util.create("%s has no ModuleInfo annotation", IllegalStateException::new);
            })
            .filter(cls -> {
              if (Platform.getPlatform().getEnvironment() == CEnvType.CLIENT)
                return true; // Every module is allowed on client.
              return ModuleHelper.getMeta(cls).env().allows(Environment.SERVER);
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

    // Create configs and files
    for (Module value : discoveredModulesByName.values()) {
      var config = this.configHandler.load(value);

      // Allow modules to modify their own configs.
      // Other modules can subscribe to this event, but this is not correct.
      ModuleHelper.runAndDropBus(
          value, BootstrapConfigEvent.ID, event -> event.bootstrapConfig(config));

      this.configHandler.save(value);
    }

    for (Module value : discoveredModulesByName.values()) {
      var config = this.configHandler.get(value);
      boolean shouldLoad = config.enabled || this.debug().isEnableAllModules();

      ModuleLoadStateEvent.Result forceState = ModuleLoadStateEvent.DEFAULT;
      var bus = ModuleLoadStateEvent.get(value);
      if (bus != null) forceState = bus.invoker().onModuleLoadState();

      switch (forceState.state()) {
        case DISABLE -> {
          if (shouldLoad)
            log.warn("Force disabling module '{}': {}", ModuleHelper.id(value), forceState.msg());
          continue;
        }
        case ENABLE -> {
          if (!shouldLoad)
            log.warn("Force Enabling module '{}': {}", ModuleHelper.id(value), forceState.msg());
        }
      }

      if (shouldLoad || forceState.state() == ForcedState.ENABLE) {
        this.modules.put(value.getClass(), value);
        this.modulesByName.put(ModuleHelper.id(value.meta()), value);
      }
    }

    for (Module module : this.loaded()) {
      this.moduleByMixinPkg.put(ModuleHelper.mixinPackage(module), module);
    }

    this.printModuleStats();

    // All modules must be available by this point.
    for (Module value : this.loaded()) {
      ModuleHelper.runAndDropBus(value, PostBootstrapEvent.ID, PostBootstrapEvent::postBootstrap);
    }
  }

  public boolean shouldApplyMixin(String mixinPkg, String mixinClassName) {
    var module = this.moduleByMixinPkg.get(mixinPkg);
    // the environment check is handled by the manager
    // when modules are first loaded.
    if (module == null) return false;
    return AndromedaMixinPlugin.shouldApply(mixinClassName);
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

  // the module manager must be initialized before any of the mixin configs
  // but any mixin config can call the manager, so we add this to every config that uses the
  // manager.
  public static void tryInit() {
    synchronized (INIT_LOCK) {
      if (instance != null) return;

      ModuleManager manager = new ModuleManager();
      instance = manager;

      try {
        manager.onInitialize();
      } catch (Throwable e) {
        manager.delayed = e;
        log.error("Failed to initialize the module manager!", e);
      }
    }
  }

  public void checkLoad() {
    if (this.delayed != null) {
      throw Util.wrap("Failed to initialize the module manager!", this.delayed);
    }
  }

  private void printModuleStats() {
    if (!this.loaded().isEmpty()) {
      Map<String, Set<Module>> categories = Utilities.supply(
          new LinkedHashMap<>(),
          map -> loaded()
              .forEach(m -> map.computeIfAbsent(m.meta().category(), s -> new LinkedHashSet<>())
                  .add(m)));

      StringBuilder builder = new StringBuilder();
      categories.forEach((s, strings) -> {
        builder.append("\n\t - ").append(s).append("\n\t  |-- ");

        StringJoiner joiner = new StringJoiner(", ");
        strings.forEach(m -> joiner.add(ModuleHelper.dotted(m)));
        builder.append(joiner);
      });

      log.info(
          "Loading Andromeda {} with {} modules: {}",
          AndromedaConstants.VERSION,
          loaded().size(),
          builder);

      var deprecated = this.loaded().stream()
          .filter(module -> module.getClass().getAnnotation(Deprecated.class) != null)
          .toList();

      if (!deprecated.isEmpty()) {
        StringJoiner joiner = new StringJoiner(", ");
        deprecated.forEach(module -> joiner.add(ModuleHelper.dotted(module)));
        log.warn(
            "Loading {} deprecated modules! Those modules might be removed without notice in a future version! Deprecated modules loading: [{}]",
            deprecated.size(),
            joiner);
      }
    } else {
      log.info("No Andromeda modules loaded! ¯\\_(ツ)_/¯");
    }
  }
}
