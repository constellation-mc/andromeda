package dev.zenfyr.andromeda.modules.entities.snowball_tweaks;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "snowball_tweaks", category = "entities", env = Environment.SERVER)
public final class Snowballs extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Snowballs() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {

    public boolean freeze = true;

    public boolean extinguish = true;

    public boolean melt = true;

    public boolean layers = false;

    public boolean enableCooldown = true;

    public int cooldown = 10;
  }
}
