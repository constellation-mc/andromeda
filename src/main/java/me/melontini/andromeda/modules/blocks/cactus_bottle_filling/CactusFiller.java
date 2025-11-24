package me.melontini.andromeda.modules.blocks.cactus_bottle_filling;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "cactus_bottle_filling", category = "blocks")
public final class CactusFiller extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  CactusFiller() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
