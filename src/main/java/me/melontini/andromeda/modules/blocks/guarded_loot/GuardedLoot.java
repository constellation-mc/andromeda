package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;

public class GuardedLoot implements Module {

    @Override
    public Class<?> configClass() {
        return Config.class;
    }
}
