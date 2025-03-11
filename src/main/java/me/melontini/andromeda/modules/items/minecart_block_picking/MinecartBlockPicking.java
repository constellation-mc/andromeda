package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "minecart_block_picking", category = "items", env = Environment.SERVER)
public final class MinecartBlockPicking extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  MinecartBlockPicking() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> PlaceBehaviorHandler::init);
    InitEvents.MAIN.listen(() -> PickUpBehaviorHandler::init);
  }

  public static final class Config extends GameConfig {
    public boolean spawnerPicking = false;
  }
}
