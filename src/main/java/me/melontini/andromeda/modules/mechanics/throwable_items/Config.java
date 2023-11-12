package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.common.collect.Lists;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.util.annotations.config.Environment;
import me.melontini.andromeda.util.annotations.config.FeatureEnvironment;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = Lists.newArrayList();

    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean canZombiesThrowItems = true;

    @ConfigEntry.Gui.Tooltip
    public int zombieThrowInterval = 40;
}
