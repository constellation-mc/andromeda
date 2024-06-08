package me.melontini.andromeda.modules.blocks.bed.safe;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "bed/safe", category = "blocks")
public final class Safe extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    Safe() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
