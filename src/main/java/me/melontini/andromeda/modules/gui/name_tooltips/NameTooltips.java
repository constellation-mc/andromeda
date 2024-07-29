package me.melontini.andromeda.modules.gui.name_tooltips;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

import java.util.function.Predicate;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "name_tooltips", category = "gui", environment = Environment.CLIENT)
public final class NameTooltips extends Module {

  NameTooltips() {
    Predicate<ModuleManager> iceberg = (manager) ->
        testModVersion(this, "minecraft", ">=1.20") && testModVersion(this, "iceberg", "<1.1.13");

    ConfigEvent.bootstrap(this).listen((moduleManager, config) -> {
      if (iceberg.test(moduleManager)) config.enabled = false;
    });
    BlockadesEvent.BUS.listen((manager, blockade) -> {
      blockade.explain(this, "enabled", iceberg, blockade.andromeda("iceberg"));
    });
  }
}
