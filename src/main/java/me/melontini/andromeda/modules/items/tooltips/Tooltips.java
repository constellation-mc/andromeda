package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "tooltips", category = "items", environment = Environment.CLIENT)
public class Tooltips extends Module<Tooltips.Config> {

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean clock = true;

        @ConfigEntry.Gui.Tooltip
        public boolean compass = true;
    }
}
