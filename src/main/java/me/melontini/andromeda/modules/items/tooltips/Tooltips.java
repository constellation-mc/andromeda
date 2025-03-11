package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "tooltips", category = "items", env = Environment.CLIENT)
public final class Tooltips extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Tooltips() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CONFIG);
  }

  public static final class Config extends BaseConfig {
    public boolean clock = true;
    public boolean compass = true;
    public boolean recoveryCompass = true;
  }
}
