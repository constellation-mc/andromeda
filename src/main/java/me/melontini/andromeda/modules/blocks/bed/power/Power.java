package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;

@ModuleInfo(name = "bed/power", category = "blocks", environment = Environment.SERVER)
public class Power extends Module<Power.Config> {

    public static class Config extends BaseConfig {
        public float power = 5.0F;
    }
}
