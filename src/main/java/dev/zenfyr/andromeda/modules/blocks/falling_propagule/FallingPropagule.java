package dev.zenfyr.andromeda.modules.blocks.falling_propagule;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "falling_propagule", category = "blocks", env = Environment.SERVER)
public final class FallingPropagule extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  FallingPropagule() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
