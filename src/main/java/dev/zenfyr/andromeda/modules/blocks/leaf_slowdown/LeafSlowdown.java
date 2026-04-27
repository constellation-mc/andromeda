package dev.zenfyr.andromeda.modules.blocks.leaf_slowdown;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@Deprecated
@ModuleInfo(name = "leaf_slowdown", category = "blocks", env = Environment.SERVER)
public final class LeafSlowdown extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  LeafSlowdown() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
    InitEvents.MAIN.listen(() -> Main::init);
  }
}
