package dev.zenfyr.andromeda.modules.misc.unknown;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;

// TODO: this module is fun and all, but maybe it's best to split the rose and remove it.
@ModuleInfo(name = "unknown", category = "misc")
public final class Unknown extends Module implements PostBootstrapEvent {

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> RoseOfTheValley::init);
    InitEvents.CLIENT.listen(() -> RoseOfTheValley::onClient);
  }
}
