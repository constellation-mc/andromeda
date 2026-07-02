package dev.zenfyr.andromeda.modules.misc.creative_mode_tab;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.event.CreateBootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.client.CreativeModeTabClient;

@ModuleInfo(name = "creative_mode_tab", category = "misc")
public class CreativeModeTab extends Module {

  CreativeModeTab() {
    CreateBootstrapConfigEvent.get(this).listen(() -> true);

    InitEvents.MAIN.listen(() -> CreativeModeTabMain::init);
    InitEvents.CLIENT.listen(() -> CreativeModeTabClient::init);
  }
}
