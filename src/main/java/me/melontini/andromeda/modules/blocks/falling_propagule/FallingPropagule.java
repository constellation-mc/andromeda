package me.melontini.andromeda.modules.blocks.falling_propagule;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class FallingPropagule implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

}
