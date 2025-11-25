package dev.zenfyr.andromeda.common.config;

import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class GameConfig extends BaseConfig {

  @ConfigEntry.Gui.Excluded
  public boolean available = true;
}
