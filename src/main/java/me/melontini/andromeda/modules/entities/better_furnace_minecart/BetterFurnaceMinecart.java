package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip(3)
@ModuleInfo(name = "better_furnace_minecart", category = "entities", environment = Environment.SERVER)
public class BetterFurnaceMinecart extends Module<BetterFurnaceMinecart.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {
        @ConfigEntry.Gui.Tooltip
        public int maxFuel = 45000;

        @ConfigEntry.Gui.Tooltip
        public boolean takeFuelWhenLow = true;
    }
}
