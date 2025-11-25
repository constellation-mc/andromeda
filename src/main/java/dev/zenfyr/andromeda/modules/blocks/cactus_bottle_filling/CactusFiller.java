package dev.zenfyr.andromeda.modules.blocks.cactus_bottle_filling;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "cactus_bottle_filling", category = "blocks")
public final class CactusFiller extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  CactusFiller() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
