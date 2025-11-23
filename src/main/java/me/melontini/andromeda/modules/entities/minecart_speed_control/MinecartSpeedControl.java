package me.melontini.andromeda.modules.entities.minecart_speed_control;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "minecart_speed_control", category = "entities")
public final class MinecartSpeedControl extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  MinecartSpeedControl() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static class Config extends GameConfig {
    public double modifier = 1d;
    public double furnaceModifier = 1d;
    public int additionalFurnaceFuel = 0;
  }
}
