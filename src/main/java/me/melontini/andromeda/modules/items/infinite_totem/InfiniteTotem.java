package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;

public class InfiniteTotem implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
