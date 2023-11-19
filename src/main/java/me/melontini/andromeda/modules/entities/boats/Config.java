package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.BOTH)
    public boolean isFurnaceBoatOn = false;

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.BOTH)
    public boolean isTNTBoatOn = false;

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.BOTH)
    public boolean isJukeboxBoatOn = false;

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.BOTH)
    public boolean isHopperBoatOn = false;
}
