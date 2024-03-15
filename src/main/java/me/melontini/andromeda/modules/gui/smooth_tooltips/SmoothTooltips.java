package me.melontini.andromeda.modules.gui.smooth_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "smooth_tooltips", category = "gui", environment = Environment.CLIENT)
public class SmoothTooltips extends Module<SmoothTooltips.Config> {

    public static class Config extends BaseConfig {
        public int clampX = 30;
        public int clampY = 30;
        public double deltaX = 0.3;
        public double deltaY = 0.3;
    }
}
