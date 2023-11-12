package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Power implements Module {

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}
