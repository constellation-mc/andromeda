package dev.zenfyr.andromeda.bootstrap;

import dev.zenfyr.andromeda.bootstrap.config.ModInitConfig;
import dev.zenfyr.andromeda.bootstrap.config.handler.BootstrapConfigHandler;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import dev.zenfyr.andromeda.bootstrap.event.BootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.event.PostModuleInitEvent;
import dev.zenfyr.andromeda.bootstrap.util.Debug;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.bootstrap.util.NetUtils;
import dev.zenfyr.andromeda.bootstrap.util.mixin.AndromedaMixinPlugin;
import dev.zenfyr.andromeda.modules.ModuleDiscovery;
import dev.zenfyr.andromeda.util.*;
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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

@CustomLog
@Accessors(fluent = true)
public class ModuleManager {

  private static final Object INIT_LOCK = new Object();

  @Getter
  private final ModContainer modContainer =
      FabricLoader.getInstance().getModContainer(AndromedaConstants.MODID).orElseThrow();

  @Getter
  private final ModConfigHandler modConfig;

  @Getter
  private final ModInitConfig modInitConfig;

  @Getter
  private final NetUtils netUtils;

  @Getter
  private final Debug debug;

  @Getter
  private final BootstrapConfigHandler configHandler = new BootstrapConfigHandler();

  @Getter
  private final InstanceDataHolder dataHolder = InstanceDataHolder.load();

  private final Map<Class<?>, Module> discoveredModules = new IdentityHashMap<>();
  private final Map<String, Module> discoveredModulesByName = new LinkedHashMap<>();

  private final Map<Class<?>, Module> modules = new IdentityHashMap<>();
  private final Map<String, Module> modulesByName = new LinkedHashMap<>();
  private final Map<String, Module> moduleByMixinPkg = new HashMap<>();

  private static ModuleManager instance;

  public ModuleManager() {
    this.modConfig = ModConfigHandler.load();
    this.modInitConfig = this.modConfig().get(ModInitConfig.KEY);
    this.netUtils = this.modConfig().get(NetUtils.KEY);
    this.debug = this.modConfig().get(Debug.KEY);
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

    this.modConfig().save();
    this.netUtils().initialize(this);

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

    for (Module value : discoveredModulesByName.values()) {
      var config = this.configHandler.load(value);

      // Allow modules to modify their own configs.
      // Other modules can subscribe to this event, but this is not correct.
      ModuleHelper.runAndDropBus(
          value, BootstrapConfigEvent.ID, event -> event.bootstrapConfig(config));

      if (config.enabled || this.debug().isEnableAllModules()) {
        this.modules.put(value.getClass(), value);
        this.modulesByName.put(ModuleHelper.id(value.meta()), value);
      }
      this.configHandler.save(value);
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
        throw new RuntimeException("Failed to initialize the module manager!", e);
      }
    }
  }

  private void printModuleStats() {
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
    if (!categories.isEmpty()) {
      log.info("Loading {} modules: {}", loaded().size(), builder);
    } else {
      log.info("No modules loaded!");
    }
  }
}
