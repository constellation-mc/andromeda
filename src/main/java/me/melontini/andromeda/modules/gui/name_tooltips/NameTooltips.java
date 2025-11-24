package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.BootstrapConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.util.Debug;

@ModuleInfo(name = "name_tooltips", category = "gui", env = Environment.CLIENT)
public final class NameTooltips extends Module {

  NameTooltips() {
    boolean loaded = Debug.get().testModVersion(this, "minecraft", ">=1.20")
        && Debug.get().testModVersion(this, "iceberg", "<1.1.13");

    if (loaded) {
      BootstrapConfigEvent.get(this).listen(config -> config.enabled = false);

      // BlockadesEvent.BUS.listen((manager, blockade) -> {
      //  blockade.explain(this, "enabled", iceberg, blockade.andromeda("iceberg"));
      // });
    }
  }
}
