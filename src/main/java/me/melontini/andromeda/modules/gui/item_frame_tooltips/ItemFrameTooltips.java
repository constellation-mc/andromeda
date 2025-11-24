package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.BootstrapConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.util.Debug;

@ModuleInfo(name = "item_frame_tooltips", category = "gui", env = Environment.CLIENT)
public final class ItemFrameTooltips extends Module implements PostBootstrapEvent {

  ItemFrameTooltips() {
    boolean loaded = Debug.get().testModVersion(this, "minecraft", ">=1.20")
        && Debug.get().testModVersion(this, "iceberg", "<1.1.13");

    if (loaded) {
      BootstrapConfigEvent.get(this).listen(config -> config.enabled = false);

      // BlockadesEvent.BUS.listen((manager, blockade) -> {
      //  blockade.explain(this, "enabled", iceberg, blockade.andromeda("iceberg"));
      // });
    }
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> Client::new);
  }
}
