package me.melontini.andromeda.modules.blocks.cactus_bottle_filling;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "cactus_bottle_filling", category = "blocks")
public final class CactusFiller extends Module {

  public static final ConfigDefinition<Module.GameConfig> CONFIG =
      new ConfigDefinition<>(() -> Module.GameConfig.class);

  CactusFiller() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }
}
