package me.melontini.andromeda.common.config;

import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class GameConfig extends BaseConfig {

  @ConfigEntry.Gui.Excluded
  public boolean active = false;
}
