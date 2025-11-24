package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.util.CommanderSupport;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;

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
