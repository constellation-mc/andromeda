package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean randomness = true;

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean recipe = true; //Used in JSON
}
