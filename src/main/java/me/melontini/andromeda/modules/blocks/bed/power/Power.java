package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "bed/power", category = "blocks", env = Environment.SERVER)
public final class Power extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Power() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static class Config extends GameConfig {
    public DoubleIntermediary power = DoubleIntermediary.of(5);
  }
}
