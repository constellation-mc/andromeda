package me.melontini.andromeda.modules.entities.villagers_follow_emeralds;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "villagers_follow_emeralds", category = "entities", env = Environment.SERVER)
public final class VillagersFollowEmeralds extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  VillagersFollowEmeralds() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
