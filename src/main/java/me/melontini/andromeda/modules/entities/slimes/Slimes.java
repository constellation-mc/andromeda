package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "slimes", category = "entities")
public final class Slimes extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Slimes() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {

    public boolean flee = true;

    public boolean merge = true;

    public int maxMerge = 4;

    public boolean slowness = false;
  }
}
