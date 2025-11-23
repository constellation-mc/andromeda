package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "minor_inconvenience", category = "misc")
public final class MinorInconvenience extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  MinorInconvenience() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
