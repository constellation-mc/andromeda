package dev.zenfyr.andromeda.modules.misc.minor_inconvenience;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "minor_inconvenience", category = "misc")
public final class MinorInconvenience extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  MinorInconvenience() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
