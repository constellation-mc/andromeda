package dev.zenfyr.andromeda.modules.world.falling_beenests;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "falling_beenests", category = "world", env = Environment.SERVER)
public final class CanBeeNestsFall extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  CanBeeNestsFall() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
