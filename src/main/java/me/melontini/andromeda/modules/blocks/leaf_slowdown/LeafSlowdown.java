package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class LeafSlowdown implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(Content.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().leafSlowdown;
    }
}
