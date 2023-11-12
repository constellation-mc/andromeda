package me.melontini.andromeda.modules.entities.snowball_tweaks;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Snowballs implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().villagersFollowEmeraldBlocks;
    }
}
