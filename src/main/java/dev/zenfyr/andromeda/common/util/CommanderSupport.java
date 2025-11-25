package dev.zenfyr.andromeda.common.util;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.event.BootstrapConfigEvent;
import dev.zenfyr.andromeda.util.Util;
import net.fabricmc.loader.api.FabricLoader;

public class CommanderSupport {

  private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("commander");

  public static void require(Module module) {
    if (module.meta().env().isClient()) return;
    if (LOADED) return;

    BootstrapConfigEvent.get(module).listen(config -> {
      if (config.enabled) {
        throw Util.create(
            ModuleHelper.id(module) + " requires Commander to run!", IllegalStateException::new);
      }
    });
  }
}
