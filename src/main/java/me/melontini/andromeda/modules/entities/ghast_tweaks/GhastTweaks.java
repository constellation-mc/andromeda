package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "ghast_tweaks", category = "entities")
public final class GhastTweaks extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GhastTweaks() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
  }

  public static class Config extends GameConfig {
    public boolean explodeOnDeath = false;
    public float explosionPower = 4;
    public boolean fireBallsConvertObsidian = false;
  }
}
