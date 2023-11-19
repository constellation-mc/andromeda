package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;

@FeatureEnvironment(Environment.SERVER)
public class AdvancementGeneration implements BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(Helper.class);
    }
}
