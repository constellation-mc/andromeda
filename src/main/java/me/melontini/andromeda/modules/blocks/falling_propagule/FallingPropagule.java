package me.melontini.andromeda.modules.blocks.falling_propagule;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "falling_propagule", category = "blocks", environment = Environment.SERVER)
public final class FallingPropagule extends Module {

  public static final ConfigDefinition<Module.GameConfig> CONFIG =
      new ConfigDefinition<>(() -> Module.GameConfig.class);

  FallingPropagule() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }
}
