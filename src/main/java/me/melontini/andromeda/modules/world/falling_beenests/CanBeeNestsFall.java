package me.melontini.andromeda.modules.world.falling_beenests;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "falling_beenests", category = "world", environment = Environment.SERVER)
public final class CanBeeNestsFall extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    CanBeeNestsFall() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
