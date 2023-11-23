package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;

@ModuleTooltip
public class GuardedLoot extends Module<GuardedLoot.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        public int range = 4;
    }
}
