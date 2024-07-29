package me.melontini.andromeda.modules.world.crop_temperature;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;

@ModuleInfo(name = "crop_temperature", category = "world", environment = Environment.SERVER)
public final class PlantTemperature extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  PlantTemperature() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> PlantTemperatureData.init(this));
  }

  @ToString
  public static class Config extends GameConfig {
    public BooleanIntermediary affectBoneMeal = BooleanIntermediary.of(true);
  }
}
