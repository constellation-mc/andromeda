package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;

@ModuleInfo(name = "unknown", category = "misc")
public final class Unknown extends Module implements PostBootstrapEvent {

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> RoseOfTheValley::init);
    InitEvents.CLIENT.listen(() -> RoseOfTheValley::onClient);
  }
}
