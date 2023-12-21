package me.melontini.andromeda.modules.world.moist_control;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.annotations.GameRule;

@ModuleTooltip
@ModuleInfo(name = "moist_control", category = "world", environment = Environment.SERVER)
public class MoistControl extends Module<MoistControl.Config> {

    public static class Config extends BasicConfig {
        @GameRule
        public int customMoisture = 4;
    }
}