package me.melontini.andromeda.modules.world.quick_fire;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@Deprecated
@ModuleInfo(name = "quick_fire", category = "world", environment = Environment.SERVER)
public final class QuickFire extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    QuickFire() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
