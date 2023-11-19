package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;

public class ThrowableItems implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
