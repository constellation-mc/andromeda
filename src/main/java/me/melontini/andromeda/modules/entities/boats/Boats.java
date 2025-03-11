package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.modules.entities.boats.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "boats", category = "entities")
public final class Boats extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> MAIN_CONFIG =
      new ConfigDefinition<>(() -> Config.class);

  Boats() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> MAIN_CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> BoatItems::init);
    InitEvents.MAIN.listen(() -> BoatEntities::init);
    InitEvents.CLIENT.listen(() -> Client::init);
  }

  public static final class Config extends BaseConfig {

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isFurnaceBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isTNTBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isJukeboxBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isHopperBoatOn = false;
  }
}
