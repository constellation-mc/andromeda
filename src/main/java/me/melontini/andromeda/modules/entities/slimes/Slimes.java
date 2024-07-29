package me.melontini.andromeda.modules.entities.slimes;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;

@ModuleInfo(name = "slimes", category = "entities", environment = Environment.SERVER)
public final class Slimes extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Slimes() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }

  @ToString
  public static final class Config extends GameConfig {

    public BooleanIntermediary flee = BooleanIntermediary.of(true);

    public BooleanIntermediary merge = BooleanIntermediary.of(true);

    public LongIntermediary maxMerge = LongIntermediary.of(4);

    public BooleanIntermediary slowness = BooleanIntermediary.of(false);
  }
}
