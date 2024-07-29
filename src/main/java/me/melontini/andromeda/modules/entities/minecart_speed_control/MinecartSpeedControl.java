package me.melontini.andromeda.modules.entities.minecart_speed_control;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;

@ModuleInfo(
    name = "minecart_speed_control",
    category = "entities",
    environment = Environment.SERVER)
public final class MinecartSpeedControl extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  MinecartSpeedControl() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }

  @ToString
  public static class Config extends Module.GameConfig {
    public DoubleIntermediary modifier = DoubleIntermediary.of(1d);
    public DoubleIntermediary furnaceModifier = DoubleIntermediary.of(1d);
    public LongIntermediary additionalFurnaceFuel = LongIntermediary.of(0);
  }
}
