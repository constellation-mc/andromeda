package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "villager_gifting", category = "mechanics", environment = Environment.SERVER)
public final class VillagerGifting extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    VillagerGifting() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }
}
