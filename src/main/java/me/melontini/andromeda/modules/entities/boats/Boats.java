package me.melontini.andromeda.modules.entities.boats;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.entities.boats.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "boats", category = "entities")
public final class Boats extends Module {

  public static final ConfigDefinition<Config> MAIN_CONFIG =
      new ConfigDefinition<>(() -> Config.class);

  Boats() {
    this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
    InitEvent.main(this).listen(() -> () -> {
      BoatItems.init(this, Andromeda.ROOT_HANDLER.get(MAIN_CONFIG));
      BoatEntities.init(Andromeda.ROOT_HANDLER.get(MAIN_CONFIG));
    });
    InitEvent.client(this).listen(() -> Client::init);
  }

  @ToString
  public static final class Config extends BaseConfig {

    @ConfigEntry.Gui.RequiresRestart
    @SpecialEnvironment(Environment.BOTH)
    public boolean isFurnaceBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    @SpecialEnvironment(Environment.BOTH)
    public boolean isTNTBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    @SpecialEnvironment(Environment.BOTH)
    public boolean isJukeboxBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    @SpecialEnvironment(Environment.BOTH)
    public boolean isHopperBoatOn = false;
  }
}
