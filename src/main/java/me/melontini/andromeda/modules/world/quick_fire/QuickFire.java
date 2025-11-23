package me.melontini.andromeda.modules.world.quick_fire;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@Deprecated
@ModuleInfo(name = "quick_fire", category = "world")
public final class QuickFire extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  QuickFire() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
