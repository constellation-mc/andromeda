package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;

public class MinecartBlockPicking implements Module<MinecartBlockPicking.Config> {


    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {
        public boolean spawnerPicking = false;
    }
}
