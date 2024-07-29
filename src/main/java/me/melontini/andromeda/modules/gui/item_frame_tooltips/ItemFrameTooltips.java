package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

import java.util.function.Predicate;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "item_frame_tooltips", category = "gui", environment = Environment.CLIENT)
public final class ItemFrameTooltips extends Module {

  ItemFrameTooltips() {
    InitEvent.client(this).listen(() -> Client::new);
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
