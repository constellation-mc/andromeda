package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class AdvancementGeneration implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(Helper.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}
