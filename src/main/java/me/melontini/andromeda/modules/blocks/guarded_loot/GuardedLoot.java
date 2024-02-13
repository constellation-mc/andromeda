package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public class GuardedLoot extends Module<GuardedLoot.Config> {

    GuardedLoot() {
    }

    public static class Config extends BaseConfig {

        public int range = 4;

        public boolean allowLockPicking = true;
    }
}
