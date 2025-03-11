package me.melontini.andromeda.modules.blocks.bed.safe;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "bed/safe", category = "blocks")
public final class Safe extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  Safe() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
