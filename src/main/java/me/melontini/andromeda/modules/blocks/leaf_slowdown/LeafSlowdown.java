package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;

@Deprecated
@ModuleInfo(name = "leaf_slowdown", category = "blocks", env = Environment.SERVER)
public final class LeafSlowdown extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  LeafSlowdown() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}
