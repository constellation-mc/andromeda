package me.melontini.andromeda.modules.entities.zombie.all_pick_up;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "zombie/all_pick_up", category = "entities")
public final class Pickup extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  Pickup() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
