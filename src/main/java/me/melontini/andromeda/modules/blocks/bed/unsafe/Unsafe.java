package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Unsafe implements Module {

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().bedsExplodeEverywhere;
    }
}
