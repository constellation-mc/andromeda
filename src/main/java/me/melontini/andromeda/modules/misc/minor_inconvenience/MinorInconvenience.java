package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;


@ModuleInfo(name = "minor_inconvenience", category = "misc")
public final class MinorInconvenience extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    MinorInconvenience() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
