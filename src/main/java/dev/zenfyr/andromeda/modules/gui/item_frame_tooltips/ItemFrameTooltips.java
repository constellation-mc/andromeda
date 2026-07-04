package dev.zenfyr.andromeda.modules.gui.item_frame_tooltips;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "item_frame_tooltips", category = "gui", env = Environment.CLIENT)
public final class ItemFrameTooltips extends Module implements PostBootstrapEvent {

  ItemFrameTooltips() {}

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> ItemFrameTooltipsClient::new);
  }
}
