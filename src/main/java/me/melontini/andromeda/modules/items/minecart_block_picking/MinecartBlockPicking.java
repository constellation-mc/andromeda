package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class MinecartBlockPicking implements Module {

    @Override
    public boolean enabled() {
        return Config.get().minecartBlockPicking;
    }
}
