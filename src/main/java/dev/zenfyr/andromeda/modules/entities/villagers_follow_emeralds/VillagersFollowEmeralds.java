package dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "villagers_follow_emeralds", category = "entities", env = Environment.SERVER)
public final class VillagersFollowEmeralds extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  VillagersFollowEmeralds() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
