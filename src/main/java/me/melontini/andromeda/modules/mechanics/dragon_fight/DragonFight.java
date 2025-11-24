package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@me.melontini.andromeda.bootstrap.ModuleInfo(
    name = "dragon_fight",
    category = "mechanics",
    env = Environment.SERVER)
public final class DragonFight extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  DragonFight() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> EnderDragonManager::init);
  }

  public static final class Config extends BaseConfig {
    public boolean respawnCrystals = true;
    public boolean scaleHealthByMaxPlayers = false;
    public boolean shorterCrystalTrackRange = true;
    public boolean shorterSpikes = false;
  }
}
