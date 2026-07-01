package dev.zenfyr.andromeda.modules.mechanics.linkart;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

// this module is based on https://github.com/constellation-mc/Linkart and includes contributions
// from GeeTransit https://github.com/GeeTransit
// TODO: port smoothing logic to new behavior
@ModuleInfo(category = "mechanics", name = "linkart", env = Environment.SERVER)
public final class Linkart extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Linkart() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> LinkartMain::init);
  }

  public static class Config extends GameConfig {
    public int pathfindingDistance = 6;
    public float velocityMultiplier = 1F;
    public double distance = 1.2d;
    public boolean chunkloading = false;
    public int chunkloadingRadius = 3;
  }
}
