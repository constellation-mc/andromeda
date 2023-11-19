package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Lockpick implements Module<Lockpick.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public int chance = 3;

        @ConfigEntry.Gui.Tooltip
        public boolean breakAfterUse = true;

        @ConfigEntry.Gui.Tooltip
        public boolean villagerInventory = true;
    }
}
