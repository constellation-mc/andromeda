package me.melontini.andromeda.modules.items.infinite_totem;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.modules.items.infinite_totem.client.Client;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;

@ModuleInfo(name = "infinite_totem", category = "items")
public final class InfiniteTotem extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  InfiniteTotem() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> Main.init(this));
    InitEvent.client(this).listen(() -> Client::init);
  }

  @ToString
  public static final class Config extends GameConfig {
    public BooleanIntermediary enableAscension = BooleanIntermediary.of(true);
  }
}
