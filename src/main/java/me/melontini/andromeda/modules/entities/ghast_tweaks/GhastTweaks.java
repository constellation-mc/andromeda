package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "ghast_tweaks", category = "entities", env = Environment.SERVER)
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
    public BooleanIntermediary explodeOnDeath = BooleanIntermediary.of(false);
    public DoubleIntermediary explosionPower = DoubleIntermediary.of(4);
    public boolean fireBallsConvertObsidian = false;
  }
}
