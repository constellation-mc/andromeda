package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Slimes implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
