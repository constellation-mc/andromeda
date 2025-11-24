package me.melontini.andromeda.modules.world.loot_barrels;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "loot_barrels", category = "world", env = Environment.SERVER)
public final class LootBarrels extends Module implements PostBootstrapEvent {

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> LootBarrelFeature::init);
  }
}
