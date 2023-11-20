package me.melontini.andromeda.base.config;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class AndromedaConfig {

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.CLIENT)
    public boolean sendOptionalData = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean sendCrashReports = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean debugMessages = false;
}
