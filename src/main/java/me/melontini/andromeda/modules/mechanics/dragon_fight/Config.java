package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public boolean respawnCrystals = true;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean scaleHealthByMaxPlayers = false;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public boolean shorterCrystalTrackRange = true;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public boolean shorterSpikes = false;
}
