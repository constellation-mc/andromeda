package dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "furnace_minecart_tweaks", category = "entities", env = Environment.SERVER)
public final class FurnaceMinecartTweaks extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  FurnaceMinecartTweaks() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  public static final class Config extends BaseConfig {
    public int maxFuel = 45000;
    public boolean takeFuelWhenLow = true;
  }
}
