package dev.zenfyr.andromeda.modules.world.loot_barrels;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "loot_barrels", category = "world", env = Environment.SERVER)
public final class LootBarrels extends Module implements PostBootstrapEvent {

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> LootBarrelFeature::init);
  }
}
