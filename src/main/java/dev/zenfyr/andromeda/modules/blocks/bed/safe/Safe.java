package dev.zenfyr.andromeda.modules.blocks.bed.safe;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "bed/safe", category = "blocks")
public final class Safe extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  Safe() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
