package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.SERVER)
public class AdvancementGeneration implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(Helper.class);
    }
}
