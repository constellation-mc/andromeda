package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;

@ModuleInfo(name = "slimes", category = "entities", env = Environment.SERVER)
public final class Slimes extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Slimes() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {

    public BooleanIntermediary flee = BooleanIntermediary.of(true);

    public BooleanIntermediary merge = BooleanIntermediary.of(true);

    public LongIntermediary maxMerge = LongIntermediary.of(4);

    public BooleanIntermediary slowness = BooleanIntermediary.of(false);
  }
}
