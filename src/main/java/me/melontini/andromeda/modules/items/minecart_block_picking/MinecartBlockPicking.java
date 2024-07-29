package me.melontini.andromeda.modules.items.minecart_block_picking;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "minecart_block_picking", category = "items", environment = Environment.SERVER)
public final class MinecartBlockPicking extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  MinecartBlockPicking() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> {
      PlaceBehaviorHandler.init();
      PickUpBehaviorHandler.init();
    });
  }

  @ToString
  public static final class Config extends GameConfig {
    public boolean spawnerPicking = false;
  }
}
