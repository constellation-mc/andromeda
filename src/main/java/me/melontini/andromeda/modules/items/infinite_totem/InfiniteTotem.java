package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.modules.items.infinite_totem.client.Client;

@ModuleInfo(name = "infinite_totem", category = "items")
public final class InfiniteTotem extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  InfiniteTotem() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
    InitEvents.CLIENT.listen(() -> Client::init);
  }

  public static final class Config extends GameConfig {
    public BooleanIntermediary enableAscension = BooleanIntermediary.of(true);
  }
}
