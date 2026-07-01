package dev.zenfyr.andromeda.modules.entities.ghast_tweaks;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "ghast_tweaks", category = "entities", env = Environment.SERVER)
public final class GhastTweaks extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GhastTweaks() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> GhastTweaksMain::init);
  }

  public static class Config extends GameConfig {
    public boolean explodeOnDeath = false;
    public float explosionPower = 4;
    public boolean fireBallsConvertObsidian = false;
  }
}
