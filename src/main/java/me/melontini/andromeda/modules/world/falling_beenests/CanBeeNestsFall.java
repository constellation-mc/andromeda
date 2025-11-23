package me.melontini.andromeda.modules.world.falling_beenests;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "falling_beenests", category = "world")
public final class CanBeeNestsFall extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  CanBeeNestsFall() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
