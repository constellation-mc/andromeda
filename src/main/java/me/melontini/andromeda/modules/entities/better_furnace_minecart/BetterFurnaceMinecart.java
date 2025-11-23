package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;

@ModuleInfo(name = "better_furnace_minecart", category = "entities")
public final class BetterFurnaceMinecart extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BetterFurnaceMinecart() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  public static final class Config extends BaseConfig {
    public int maxFuel = 45000;
    public boolean takeFuelWhenLow = true;
  }
}
