package me.melontini.andromeda.modules.bugfixes.aligned_alternatives;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class AlignedRecipeAlternatives implements Module {

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }

    @Override
    public boolean enabled() {
        return Config.get().properlyAlignedRecipeAlternatives;
    }
}
