package me.melontini.andromeda.modules.entities.snowball_tweaks;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "snowball_tweaks", category = "entities")
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
