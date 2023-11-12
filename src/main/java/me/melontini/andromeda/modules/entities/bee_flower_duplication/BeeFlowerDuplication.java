package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class BeeFlowerDuplication implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().beeFlowerDuplication;
    }
}
