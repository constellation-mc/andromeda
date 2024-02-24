package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "bee_flower_duplication", category = "entities", environment = Environment.SERVER)
public class BeeFlowerDuplication extends Module<BeeFlowerDuplication.Config> {

    BeeFlowerDuplication() {
    }

    public static class Config extends BaseConfig {
        public boolean tallFlowers = true;
    }
}
