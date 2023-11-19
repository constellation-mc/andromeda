package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class LeafSlowdown implements Module {

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}
