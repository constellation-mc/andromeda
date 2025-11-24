package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "zombie/clean_pickup", category = "entities", env = Environment.SERVER)
public final class Pickup extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  Pickup() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
