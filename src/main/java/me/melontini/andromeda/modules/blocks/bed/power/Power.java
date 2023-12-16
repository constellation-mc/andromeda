package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;

@ModuleTooltip
@ModuleInfo(name = "bed/power", category = "blocks", environment = Environment.SERVER)
public class Power extends Module<Power.Config> {

    public static class Config extends BasicConfig {
        public float power = 5.0F;
    }
}
