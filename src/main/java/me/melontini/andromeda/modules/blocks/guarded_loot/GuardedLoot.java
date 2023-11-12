package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class GuardedLoot implements Module {

    @Override
    public boolean enabled() {
        return Config.get().guardedLoot.enabled;
    }
}
