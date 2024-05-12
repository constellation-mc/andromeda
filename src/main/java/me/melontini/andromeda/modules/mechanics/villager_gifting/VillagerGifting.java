package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "villager_gifting", category = "mechanics", environment = Environment.SERVER)
public final class VillagerGifting extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    VillagerGifting() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
