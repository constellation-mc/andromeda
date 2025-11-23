package me.melontini.andromeda.modules.world.loot_barrels;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;

@ModuleInfo(name = "loot_barrels", category = "world")
public final class LootBarrels extends Module implements PostBootstrapEvent {

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> LootBarrelFeature::init);
  }
}
