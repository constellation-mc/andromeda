package dev.zenfyr.andromeda.util;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public class Debug extends BaseConfig {

  public static final ModConfigHandler.Key<Debug> KEY =
      new ModConfigHandler.Key<>("debug", Debug.class);

  public boolean enableAllModules = false;
  public Map<String, Set<String>> skipModIntegration = new HashMap<>();

  public boolean skipIntegration(Module module, String mod) {
    var set = this.skipModIntegration.get(ModuleHelper.id(module));
    if (set == null) return false;
    return set.contains(mod);
  }

  public boolean isModLoaded(Module module, String mod) {
    return !skipIntegration(module, mod) && FabricLoader.getInstance().isModLoaded(mod);
  }

  public boolean testModVersion(Module m, String modId, String predicate) {
    Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
    if (mod.isPresent() && !skipIntegration(m, modId)) {
      try {
        VersionPredicate version = VersionPredicate.parse(predicate);
        return version.test(mod.get().getMetadata().getVersion());
      } catch (VersionParsingException e) {
        return false;
      }
    }
    return false;
  }

  public static Debug get() {
    return ModuleManager.get().modConfig().get(KEY);
  }
}
