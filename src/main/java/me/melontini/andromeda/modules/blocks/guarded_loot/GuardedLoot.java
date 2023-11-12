package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;

public class GuardedLoot implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
