package dev.zenfyr.andromeda.modules.mechanics.throwable_items;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.common.util.CommanderSupport;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.client.Client;

@ModuleInfo(name = "throwable_items", category = "mechanics")
public final class ThrowableItems extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<ClientConfig> CLIENT_CONFIG =
      new ConfigDefinition<>(() -> ClientConfig.class);
  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  ThrowableItems() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CLIENT_CONFIG);

    CommanderSupport.require(this);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
    InitEvents.CLIENT.listen(() -> Client::init);
  }

  public static class ClientConfig extends BaseConfig {
    public boolean tooltip = true;
  }

  public static class Config extends BaseConfig {
    public boolean canZombiesThrowItems = true;
    public double zombieThrowInterval = 40;
  }
}
