package me.melontini.andromeda.modules.entities.villagers_follow_emeralds;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "villagers_follow_emeralds", category = "entities", environment = Environment.SERVER)
public final class VillagersFollowEmeralds extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    VillagersFollowEmeralds() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
