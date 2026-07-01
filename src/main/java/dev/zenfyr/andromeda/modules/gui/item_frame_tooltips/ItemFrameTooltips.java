package dev.zenfyr.andromeda.modules.gui.item_frame_tooltips;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.event.BootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "item_frame_tooltips", category = "gui", env = Environment.CLIENT)
public final class ItemFrameTooltips extends Module implements PostBootstrapEvent {

  ItemFrameTooltips() {
    var debug = ModuleManager.get().debug();
    boolean loaded = debug.testModVersion(this, "minecraft", ">=1.20")
        && debug.testModVersion(this, "iceberg", "<1.1.13");

    if (loaded) {
      BootstrapConfigEvent.get(this).listen(config -> config.enabled = false);

      // BlockadesEvent.BUS.listen((manager, blockade) -> {
      //  blockade.explain(this, "enabled", iceberg, blockade.andromeda("iceberg"));
      // });
    }
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> ItemFrameTooltipsClient::new);
  }
}
