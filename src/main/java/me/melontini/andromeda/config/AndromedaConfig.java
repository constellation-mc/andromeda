package me.melontini.andromeda.config;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.util.annotations.config.ValueSwitch;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class AndromedaConfig {

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean compatMode = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.CLIENT)
    public boolean sendOptionalData = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ValueSwitch
    public boolean sendCrashReports = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @ValueSwitch
    public boolean debugMessages = false;
}
