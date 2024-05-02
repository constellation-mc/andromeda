package me.melontini.andromeda.modules.blocks.bed.safe;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "bed/safe", category = "blocks")
public class Safe extends Module {

    Safe() {
        this.defineConfig(ConfigState.GAME, ConfigDefinition.GAME);
    }
}
