package me.melontini.andromeda.modules.entities.minecart_speed_control;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "minecart_speed_control", category = "entities", environment = Environment.SERVER)
public class MinecartSpeedControl extends Module<MinecartSpeedControl.Config> {

    public static class Config extends Module.BaseConfig {
        public double modifier = 1d;
        public double furnaceModifier = 1d;
        public int additionalFurnaceFuel = 0;
    }
}
