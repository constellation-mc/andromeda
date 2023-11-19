package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.CLIENT)
public class Tooltips implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
