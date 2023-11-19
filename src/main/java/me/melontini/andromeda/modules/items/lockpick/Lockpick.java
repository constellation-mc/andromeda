package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;

public class Lockpick implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
