package dev.zenfyr.andromeda.modules.entities.boats;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.modules.entities.boats.client.Client;
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
    public boolean isFurnaceBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isTNTBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isJukeboxBoatOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isHopperBoatOn = false;
  }
}
