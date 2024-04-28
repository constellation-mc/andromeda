package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.NumberIntermediary;

@ModuleInfo(name = "bed/power", category = "blocks", environment = Environment.SERVER)
public class Power extends Module<Power.Config> {

    public static class Config extends BaseConfig {
        public NumberIntermediary power = NumberIntermediary.of(5);
    }
}
