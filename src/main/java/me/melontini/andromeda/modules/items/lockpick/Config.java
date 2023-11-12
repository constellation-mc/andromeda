package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public int chance = 3;

    @ConfigEntry.Gui.Tooltip
    public boolean breakAfterUse = true;

    @ConfigEntry.Gui.Tooltip
    public boolean villagerInventory = true;
}
