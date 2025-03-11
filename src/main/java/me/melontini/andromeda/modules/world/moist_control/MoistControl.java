package me.melontini.andromeda.modules.world.moist_control;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;

@ModuleInfo(name = "moist_control", category = "world", env = Environment.SERVER)
public final class MoistControl extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  MoistControl() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static class Config extends BaseConfig {
    public LongIntermediary customMoisture = LongIntermediary.of(4);
  }
}
