package me.melontini.andromeda.modules.blocks.campfire_effects;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.SERVER)
public class CampfireEffects implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
