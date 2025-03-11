package me.melontini.andromeda.common.util.commander;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleHelper;
import me.melontini.andromeda.bootstrap.event.BootstrapConfigEvent;
import me.melontini.andromeda.util.Util;
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
