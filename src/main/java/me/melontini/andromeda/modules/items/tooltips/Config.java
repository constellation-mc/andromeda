package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public boolean clock = true;

    @ConfigEntry.Gui.Tooltip
    public boolean compass = true;

    @ConfigEntry.Gui.Tooltip
    public boolean recoveryCompass = true;
}
