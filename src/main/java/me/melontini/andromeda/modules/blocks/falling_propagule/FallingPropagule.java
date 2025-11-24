package me.melontini.andromeda.modules.blocks.falling_propagule;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "falling_propagule", category = "blocks", env = Environment.SERVER)
public final class FallingPropagule extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  FallingPropagule() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
