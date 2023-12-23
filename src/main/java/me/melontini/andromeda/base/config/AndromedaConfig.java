package me.melontini.andromeda.base.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class AndromedaConfig {

    @ConfigEntry.Gui.RequiresRestart
    public boolean sideOnlyMode = false;

    public boolean sendCrashReports = true;
}
