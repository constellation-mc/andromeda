package dev.zenfyr.andromeda.modules.items.infinite_totem;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;
import dev.zenfyr.andromeda.modules.items.infinite_totem.client.InfiniteTotemClient;

@ModuleInfo(name = "infinite_totem", category = "items")
public final class InfiniteTotem extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  InfiniteTotem() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> InfiniteTotemMain::init);
    InitEvents.CLIENT.listen(() -> InfiniteTotemClient::init);
  }

  public static final class Config extends GameConfig {
    public boolean enableAscension = true;
  }
}
