package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public boolean flee = true;

    @ConfigEntry.Gui.Tooltip
    public boolean merge = true;

    @ConfigEntry.Gui.Tooltip
    public int maxMerge = 4;

    @ConfigEntry.Gui.Tooltip
    public boolean slowness = false;
}
