package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "slimes", category = "entities", environment = Environment.SERVER)
public class Slimes extends Module<Slimes.Config> {

    Slimes() {
    }

    public static class Config extends BaseConfig {

        public boolean flee = true;

        public boolean merge = true;

        public int maxMerge = 4;

        public boolean slowness = false;
    }
}
