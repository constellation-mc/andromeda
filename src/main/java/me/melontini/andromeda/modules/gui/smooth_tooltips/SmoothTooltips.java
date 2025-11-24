package me.melontini.andromeda.modules.gui.smooth_tooltips;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "smooth_tooltips", category = "gui", env = Environment.CLIENT)
public final class SmoothTooltips extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  SmoothTooltips() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CONFIG);
  }

  public static final class Config extends BaseConfig {
    public int clampX = 30;
    public int clampY = 30;
    public double deltaX = 0.3;
    public double deltaY = 0.3;
  }
}
