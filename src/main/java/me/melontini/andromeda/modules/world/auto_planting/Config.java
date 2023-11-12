package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public boolean blacklistMode = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> idList = Lists.newArrayList();
}
