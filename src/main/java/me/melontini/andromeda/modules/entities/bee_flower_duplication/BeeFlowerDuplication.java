package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;

@ModuleInfo(name = "bee_flower_duplication", category = "entities", env = Environment.SERVER)
public final class BeeFlowerDuplication extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BeeFlowerDuplication() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {
    public BooleanIntermediary tallFlowers = BooleanIntermediary.of(true);
  }
}
