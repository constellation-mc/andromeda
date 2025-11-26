package dev.zenfyr.andromeda.bootstrap.util.mixin;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.util.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.CustomLog;
import me.melontini.dark_matter.api.mixin.VirtualMixins;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

@CustomLog
public class MixinHandler {

  public static final String NOTICE = "mixin_processor.config_notice";
  public static final String JAVA_VERSION = "JAVA_17";
  public static final String MIXIN_VERSION = "0.8.5";

  private final ModuleManager manager;
  private final Map<String, Module> mixinConfigs = new HashMap<>();
  private final Map<String, List<String>> mixinClasses = new HashMap<>();

  public MixinHandler(ModuleManager manager) {
    this.manager = manager;
  }

  @ApiStatus.Internal
  public Optional<Module> fromConfig(String name) {
    return Optional.ofNullable(mixinConfigs.get(name));
  }

  @ApiStatus.Internal
  public List<String> mixinsFromPackage(@Nullable String pkg) {
    if (pkg == null) return Collections.emptyList();
    return mixinClasses.getOrDefault(pkg, Collections.emptyList());
  }

  public void addMixins() {
    for (Module module : manager.loaded()) {
      String pkg = ModuleHelper.mixinPackage(module);
      var list = AndromedaMixinPlugin.discoverInPackage(pkg);
      if (!list.isEmpty()) this.mixinClasses.put(pkg, list);
    }

    VirtualMixins.addMixins(acceptor -> {
      for (Module module : this.manager.loaded()) {
        if (!this.mixinClasses.containsKey(ModuleHelper.mixinPackage(module))) continue;

        JsonObject config = createConfig(module);
        String cfg = "andromeda_dynamic$$" + ModuleHelper.dotted(module);
        try (ByteArrayInputStream bais =
            new ByteArrayInputStream(config.toString().getBytes(StandardCharsets.UTF_8))) {
          acceptor.add(cfg, bais);
          this.mixinConfigs.put(cfg, module);
        } catch (IOException e) {
          throw Util.wrap("Failed to create mixin config %s".formatted(cfg), e);
        }
      }
    });

    var set = Mixins.getConfigs();
    if (set instanceof HashSet<Config> hs)
      set = ((HashSet<Config>) hs.clone()); // Maybe, hopefully will help avoid CMEs
    else {
      log.warn("Mixins.getConfigs() is not a HashSet?");
      set = ImmutableSet.copyOf(set);
    }
    set.forEach(config -> {
      if (this.mixinConfigs.containsKey(config.getName())) {
        // Decorate mixin configs to allow Fabric's fork to blame Andromeda.
        config.getConfig().decorate(FabricUtil.KEY_MOD_ID, MODID);
      }
    });
  }

  public JsonObject createConfig(Module module) {
    JsonObject object = new JsonObject();
    object.addProperty("required", true);
    object.addProperty("minVersion", MIXIN_VERSION);
    object.addProperty("package", module.getClass().getPackageName() + ".mixin");
    object.addProperty("compatibilityLevel", JAVA_VERSION);
    object.addProperty("plugin", ModuleMixinPlugin.class.getName());
    object.addProperty("refmap", "andromeda-refmap.json");
    JsonObject injectors = new JsonObject();
    injectors.addProperty("defaultRequire", 1);
    injectors.addProperty("maxShiftBy", 3);
    object.add("injectors", injectors);

    return object;
  }
}
