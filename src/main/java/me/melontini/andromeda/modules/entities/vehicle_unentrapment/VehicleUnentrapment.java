package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "vehicle_unentrapment", category = "entities")
public final class VehicleUnentrapment extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  VehicleUnentrapment() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
  }
}
