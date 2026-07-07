package dev.zenfyr.andromeda.modules.items.magnet;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.modules.items.magnet.client.MagnetClient;

@ModuleInfo(name = "magnet", category = "items")
public final class Magnet extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Magnet() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> MagnetItem::init);
    InitEvents.CLIENT.listen(() -> MagnetClient::init);
  }

  public static class Config extends BaseConfig {
    public double rangeMultiplier = 5;
  }
}
