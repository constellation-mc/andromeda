package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;

@Unscoped
@ModuleInfo(name = "minecart_block_picking", category = "items")
public class MinecartBlockPicking extends Module<MinecartBlockPicking.Config> {

    MinecartBlockPicking() {
    }

    public static class Config extends BaseConfig {

        public boolean spawnerPicking = false;
    }
}
