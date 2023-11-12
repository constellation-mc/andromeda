package me.melontini.andromeda.modules.bugfixes.advancement_shadow;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class AdvancementShadow implements Module {

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }

    @Override
    public boolean enabled() {
        return Config.get().frameIndependentAdvancementShadow;
    }
}
