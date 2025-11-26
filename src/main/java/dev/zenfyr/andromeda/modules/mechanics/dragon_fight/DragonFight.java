package dev.zenfyr.andromeda.modules.mechanics.dragon_fight;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "dragon_fight", category = "mechanics", env = Environment.SERVER)
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
