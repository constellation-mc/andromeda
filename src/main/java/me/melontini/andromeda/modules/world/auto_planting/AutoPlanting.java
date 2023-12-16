package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.Origin;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Origin(mod = "TinyTweaks", author = "HephaestusDev")
@ModuleTooltip(2)
@ModuleInfo(name = "auto_planting", category = "world", environment = Environment.SERVER)
public class AutoPlanting extends Module<AutoPlanting.Config> {

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean blacklistMode = true;

        @ConfigEntry.Gui.Tooltip
        public List<String> idList = Lists.newArrayList();
    }
}
