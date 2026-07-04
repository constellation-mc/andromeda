package dev.zenfyr.andromeda.modules.world.quick_fire;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.bootstrap.util.Launchpad;
import dev.zenfyr.andromeda.common.config.GameConfig;

@Deprecated
@ModuleInfo(name = "quick_fire", category = "world", env = Environment.SERVER)
public final class QuickFire extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  QuickFire() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
    Launchpad.forModule(this);
  }
}
