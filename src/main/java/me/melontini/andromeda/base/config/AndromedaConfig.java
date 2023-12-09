package me.melontini.andromeda.base.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class AndromedaConfig {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean sideOnlyMode = false;

    @ConfigEntry.Gui.Tooltip
    public boolean sendCrashReports = true;

    @ConfigEntry.Gui.Tooltip
    public boolean debugMessages = false;
}
