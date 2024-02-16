package me.melontini.andromeda.modules.world.moist_control;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "moist_control", category = "world", environment = Environment.SERVER)
public class MoistControl extends Module<MoistControl.Config> {

    public static class Config extends BaseConfig {
        public int customMoisture = 4;
    }
}