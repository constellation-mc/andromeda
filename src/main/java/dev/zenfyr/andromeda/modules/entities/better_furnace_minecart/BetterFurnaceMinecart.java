package dev.zenfyr.andromeda.modules.entities.better_furnace_minecart;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "better_furnace_minecart", category = "entities", env = Environment.SERVER)
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
