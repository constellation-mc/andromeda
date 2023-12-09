package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "minecart_block_picking", category = "items")
public class MinecartBlockPicking extends Module<MinecartBlockPicking.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean spawnerPicking = false;
    }
}
