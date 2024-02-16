package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;

@Unscoped
@ModuleInfo(name = "incubator", category = "blocks")
public class Incubator extends Module<Incubator.Config> {

    Incubator() {
    }

    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}
