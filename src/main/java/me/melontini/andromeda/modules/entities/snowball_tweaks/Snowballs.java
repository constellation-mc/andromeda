package me.melontini.andromeda.modules.entities.snowball_tweaks;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;

@ModuleInfo(name = "snowball_tweaks", category = "entities", env = Environment.SERVER)
public final class Snowballs extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Snowballs() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {

    public BooleanIntermediary freeze = BooleanIntermediary.of(true);

    public BooleanIntermediary extinguish = BooleanIntermediary.of(true);

    public BooleanIntermediary melt = BooleanIntermediary.of(true);

    public BooleanIntermediary layers = BooleanIntermediary.of(false);

    public BooleanIntermediary enableCooldown = BooleanIntermediary.of(true);

    public LongIntermediary cooldown = LongIntermediary.of(10);
  }
}
