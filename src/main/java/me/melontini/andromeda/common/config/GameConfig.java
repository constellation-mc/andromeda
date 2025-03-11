package me.melontini.andromeda.common.config;

import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class GameConfig extends BaseConfig {

  @ConfigEntry.Gui.Excluded
  public BooleanIntermediary available = BooleanIntermediary.of(true);
}
