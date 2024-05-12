package me.melontini.andromeda.modules.entities.zombie.all_pick_up;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "zombie/all_pick_up", category = "entities", environment = Environment.SERVER)
public final class Pickup extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    Pickup() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
